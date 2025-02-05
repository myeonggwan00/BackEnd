package com.auction.auction_site.service;

import com.auction.auction_site.dto.payment.PaymentRequest;
import com.auction.auction_site.entity.Product;
import com.auction.auction_site.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final String WIDGET_SECRET_KEY = "test_gsk_docs_OaPz8L5KdmQXkzRz3y47BMw6";
    private static final String PAYMENT_CONFIRM_URL = "https://api.tosspayments.com/v1/payments/confirm";
    private final ProductRepository productRepository;

    @Transactional
    public ResponseEntity<?> confirmPayment(PaymentRequest paymentRequest) {
        Map<String, Object> response = new HashMap<>();
        try { // 결제 승인 요청
            JSONObject obj = new JSONObject();

            obj.put("orderId", paymentRequest.getOrderId());
            obj.put("amount", paymentRequest.getAmount());
            obj.put("paymentKey", paymentRequest.getPaymentKey());

            String authorizations = getAuthorizationHeader();
            HttpURLConnection connection = createHttpConnection(authorizations);
            sendRequest(connection, obj);

            int code = connection.getResponseCode();
            boolean isSuccess = code == 200;

            InputStream responseStream = isSuccess ? connection.getInputStream() : connection.getErrorStream();
            JSONObject responseJson = parseResponse(responseStream);
            responseStream.close();

            if (isSuccess) {
                updateProductStatus(paymentRequest.getProductId());
                response.put("status", "success");
                response.put("message", "결제 및 상품 상태 업데이트 성공");
                response.put("paymentData", responseJson);
                response.put("productStatusUpdated", true);
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "fail");
                response.put("message", "결제 승인 실패");
                response.put("errorData", responseJson);
                return ResponseEntity.status(code).body(response);
            }
        } catch (IOException | ParseException e) {
            logger.error("결제 승인 요청 실패", e);
            response.put("status", "error");
            response.put("message", "서버 오류 발생");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    private void updateProductStatus(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다: " + productId));
        product.setProductStatus(false); // 결제 성공시 상품상태 false
        productRepository.save(product);


    }

    private String getAuthorizationHeader() {
        Base64.Encoder encoder = Base64.getEncoder();
        byte[] encodedBytes = encoder.encode((WIDGET_SECRET_KEY + ":").getBytes(StandardCharsets.UTF_8));
        return "Basic " + new String(encodedBytes);
    }

    private HttpURLConnection createHttpConnection(String authorization) throws IOException {
        URL url = new URL(PAYMENT_CONFIRM_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Authorization", authorization);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        return connection;
    }

    private void sendRequest(HttpURLConnection connection, JSONObject requestBody) throws IOException {
        try (OutputStream outputStream = connection.getOutputStream()) {
            outputStream.write(requestBody.toString().getBytes(StandardCharsets.UTF_8));
        }
    }

    private JSONObject parseResponse(InputStream responseStream) throws IOException, ParseException {
        try (Reader reader = new InputStreamReader(responseStream, StandardCharsets.UTF_8)) {
            JSONParser parser = new JSONParser();
            return (JSONObject) parser.parse(reader);
        }
    }

}

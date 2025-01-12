package com.auction.auction_site.controller;

import com.auction.auction_site.dto.ErrorResponse;
import com.auction.auction_site.dto.SuccessResponse;
import com.auction.auction_site.dto.product.ProductRequestDto;
import com.auction.auction_site.dto.product.ProductResponseDto;
import com.auction.auction_site.security.oauth.CustomOAuth2User;
import com.auction.auction_site.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    /**
     * 상품 등록
     */
    @PostMapping
    public ResponseEntity<SuccessResponse> createProduct(@RequestBody ProductRequestDto productRequestDto) {

        String loginId = ((CustomOAuth2User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getLoginId();
        //   System.out.println("Principal Type: " + principal.getClass().getName());
        if (loginId == null || loginId.trim().isEmpty()) {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setStatus("FAIL");
            errorResponse.setCode("Unauthorized");
            errorResponse.setMessage("로그인 정보가 없습니다.");
        }

        ProductResponseDto createdProduct = productService.createProduct(productRequestDto, loginId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(SuccessResponse.success("상품이 성공적으로 생성되었습니다.", createdProduct));

    }
    /**
     * 전체 상품 상세보기
     */

    @GetMapping
    public List<ProductResponseDto> ProductList(){
        return productService.productList();
    }


    /**
     * 특정 상품 상세보기
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDto>ProductDetail(@PathVariable Long id){

        ProductResponseDto productDetail = productService.productDetail(id);
        return ResponseEntity.status(HttpStatus.OK).body(productDetail);
    }

    /**
     상품 수정
     */
    @PatchMapping("/{id}")
    public ResponseEntity<SuccessResponse>productUpdate(@PathVariable Long id, @RequestBody ProductRequestDto dto){
        String loginId = ((CustomOAuth2User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getLoginId();
        if (loginId == null || loginId.trim().isEmpty()) {

            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setStatus("FAIL");
            errorResponse.setCode("Unauthorized");
            errorResponse.setMessage("로그인 정보가 없습니다.");

        }

        ResponseEntity<?> updatedProduct = productService.productUpdate(id, loginId, dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(SuccessResponse.success("상품이 성공적으로 수정되었습니다.", updatedProduct));

    }


    /**
     * 상품 삭제
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> productDelete(@PathVariable Long id) {
        // 인증 정보 확인
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomOAuth2User)) {
            throw new IllegalStateException("유효하지 않은 인증 정보입니다.");
        }

        // 로그인 ID 가져오기
        String loginId = ((CustomOAuth2User) authentication.getPrincipal()).getLoginId();

        // 서비스 호출
        return productService.deleteProduct(id, loginId);

    }


}

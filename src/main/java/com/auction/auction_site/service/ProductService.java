package com.auction.auction_site.service;

import com.auction.auction_site.dto.ErrorResponse;
import com.auction.auction_site.dto.product.ProductRequestDto;
import com.auction.auction_site.dto.product.ProductResponseDto;
import com.auction.auction_site.entity.Member;
import com.auction.auction_site.entity.Product;
import com.auction.auction_site.repository.MemberRepository;
import com.auction.auction_site.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private MemberRepository memberRepository;

    /**
     * 상품 등록
     */
    public ProductResponseDto createProduct(ProductRequestDto dto, String loginId){

        Member member = memberRepository.findByLoginId(loginId);
        // 유저를 찾을 수 없는 경우
        if (member == null) {
            throw new  IllegalStateException("유효하지 않은 인증 정보입니다.");
        }
        Product product = Product.builder()
                .productName(dto.getProductName()).productDetail(dto.getProductDetail()).startPrice(dto.getStartPrice())
                .bidStep(dto.getBidStep()).auctionEndDate(dto.getAuctionEndDate()).member(member)
                .build();

        Product savedProduct = productRepository.save(product);

        return ProductResponseDto.builder()
                .id(savedProduct.getId()).productName(savedProduct.getProductName()).productDetail(savedProduct.getProductDetail())
                .startPrice(savedProduct.getStartPrice()).bidStep(savedProduct.getBidStep()).auctionEndDate(savedProduct.getAuctionEndDate())
                .createdAt(savedProduct.getCreatedAt()).updatedAt(savedProduct.getUpdatedAt()).viewCount(savedProduct.getViewCount())
                .productStatus(savedProduct.getProductStatus()).build();
    }


    /**
     * 전체 상품 리스트
     */
    public List<ProductResponseDto> productList() {
        return productRepository.findAll().stream()
                .map(product -> new ProductResponseDto(
                        product.getId(),
                        product.getProductName(),
                        product.getProductDetail(),
                        product.getStartPrice(),
                        product.getBidStep(),
                        product.getAuctionEndDate(),
                        product.getProductStatus(),
                        product.getCreatedAt(),
                        product.getUpdatedAt(),
                        product.getViewCount()
                )).collect(Collectors.toList());
    }

    /**
     * 상품 상세
     */
    public ProductResponseDto productDetail(Long id) {
        Product product = productRepository.findById(id).orElseThrow(()->new BadCredentialsException("상품 정보를 찾을 수 없습니다."));
        return ProductResponseDto.builder()
                .id(product.getId()).productName(product.getProductName()).productDetail(product.getProductDetail())
                .startPrice(product.getStartPrice()).bidStep(product.getBidStep()).auctionEndDate(product.getAuctionEndDate())
                .createdAt(product.getCreatedAt()).updatedAt(product.getUpdatedAt()).viewCount(product.getViewCount())
                .productStatus(product.getProductStatus()).build();
    }

    /**
     * 상품 수정
     */
    public ResponseEntity<?> productUpdate(Long id, String loginId, ProductRequestDto dto){
        ErrorResponse errorResponse = new ErrorResponse();
        Member member = memberRepository.findByLoginId(loginId);
        // 유저를 찾을 수 없는 경우
        if (loginId == null || loginId.trim().isEmpty()) {
            errorResponse.setStatus("FAIL");
            errorResponse.setMessage("유효하지 않은 인증 정보입니다.");
            errorResponse.setCode("UNAUTHORIZED");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }

        // 상품 찾기
        Optional<Product> productOptional = productRepository.findById(id);
        if (!productOptional.isPresent()) {
            errorResponse.setStatus("FAIL");
            errorResponse.setMessage("해당 상품을 찾을 수 없습니다.");
            errorResponse.setCode("NOT_FOUND");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }

        Product product = productOptional.get();

        // 유저와 상품의 소유자가 동일한지 체크
        if (!product.getMember().getId().equals(member.getId())) {
            errorResponse.setStatus("FAIL");
            errorResponse.setMessage("해당 상품을 수정할 권한이 없습니다.");
            errorResponse.setCode("FORBIDDEN");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        }


        // 수정할 데이터 부분만 업데이트
        if (dto.getProductName() != null) {
            product.setProductName(dto.getProductName());
        }
        if (dto.getProductDetail() != null) {
            product.setProductDetail(dto.getProductDetail());
        }
        if (dto.getStartPrice() != null) {
            product.setStartPrice(dto.getStartPrice());
        }
        if (dto.getBidStep() != null) {
            product.setBidStep(dto.getBidStep());
        }
        if (dto.getAuctionEndDate() != null) {
            product.setAuctionEndDate(dto.getAuctionEndDate());
        }

        Product updatedProduct = productRepository.save(product);

        ProductResponseDto responseDto = ProductResponseDto.builder()
                .id(updatedProduct.getId())
                .productName(updatedProduct.getProductName())
                .productDetail(updatedProduct.getProductDetail())
                .startPrice(updatedProduct.getStartPrice())
                .bidStep(updatedProduct.getBidStep())
                .auctionEndDate(updatedProduct.getAuctionEndDate())
                .createdAt(updatedProduct.getCreatedAt())
                .updatedAt(updatedProduct.getUpdatedAt())
                .viewCount(updatedProduct.getViewCount())
                .productStatus(updatedProduct.getProductStatus())
                .build();

        return ResponseEntity.ok(responseDto);
    }

    /**
     * 상품 삭제
     */
    public ResponseEntity<?> deleteProduct(Long id, String loginId) {
        // 회원 정보 확인
        Member member = memberRepository.findByLoginId(loginId);
        if (loginId == null || loginId.trim().isEmpty() || member == null) {
            throw new IllegalStateException("유효하지 않은 인증 정보입니다.");
        }

        Optional<Product> productOptional = productRepository.findById(id);

        ErrorResponse errorResponse = new ErrorResponse();
        // 상품 확인
        if (!productOptional.isPresent()) {
            errorResponse.setStatus("FAIL");
            errorResponse.setMessage("해당 상품을 찾을 수없습니다.");
            errorResponse.setCode("NOT_FOUND");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        }

        Product product = productOptional.get();

        // 상품 소유자 확인
        if (!product.getMember().getId().equals(member.getId())) {
            errorResponse.setStatus("FAIL");
            errorResponse.setMessage("상품 삭제 권한이 없습니다.");
            errorResponse.setCode("UNAUTHORIZED");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }


        // 상품 삭제
        productRepository.delete(product);
        errorResponse.setStatus("SUCCESS");
        errorResponse.setMessage("상품이 삭제되었습니다.");

        // 성공 응답 반환
        return ResponseEntity.ok().body(errorResponse);
    }
}

package com.auction.auction_site.service;

import com.auction.auction_site.dto.ErrorResponse;
import com.auction.auction_site.dto.SuccessResponse;
import com.auction.auction_site.entity.Member;
import com.auction.auction_site.entity.MemberResetToken;
import com.auction.auction_site.exception.EntityNotFound;
import com.auction.auction_site.repository.MemberRepository;
import com.auction.auction_site.repository.MemberResetTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {


    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private MemberResetTokenRepository ResetTokenRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private final BCryptPasswordEncoder bCryptPasswordEncoder;


    /**
     *  이메일 확인
     */
    public ResponseEntity<?> findMemberByEmail(String email) {
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new EntityNotFound("이메일에 해당하는 사용자를 찾을 수 없습니다."));

        return ResponseEntity.ok(member);
    }


    /**
     * uuid 토큰 생성, 이메일을 db에 저장
     */
    public String createPasswordResetToken(String email) {
        String token = UUID.randomUUID().toString();

        MemberResetToken resetToken = new MemberResetToken();
        resetToken.setToken(token);
        resetToken.setEmail(email);
        resetToken.setExpirationDate(LocalDateTime.now().plusHours(1)); // 1시간 유효

        ResetTokenRepository.save(resetToken);

        return token;
    }

    /**
     * 비밀번호 재설정
     */
    public ResponseEntity<?>  resetPassword(String email, String token, String newPassword) {

        ErrorResponse errorResponse = new ErrorResponse();
        SuccessResponse successResponse = new SuccessResponse();

        MemberResetToken resetToken = ResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("유효하지 않은 토큰입니다."));

        if (!resetToken.getEmail().equals(email)) {
            errorResponse.setStatus("FAIL");
            errorResponse.setMessage("이메일이 토큰과 일치하지 않습니다.");
            errorResponse.setCode("UNAUTHORIZED");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }

        if (resetToken.getExpirationDate().isBefore(LocalDateTime.now())) {
            errorResponse.setStatus("FAIL");
            errorResponse.setMessage("토큰이 만료되었습니다.");
            errorResponse.setCode("FORBIDDEN");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);

        }

        // 비밀번호 변경
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new EntityNotFound("이메일에 해당하는 사용자를 찾을 수 없습니다."));
        member.updatePassword(bCryptPasswordEncoder.encode(newPassword));
        memberRepository.save(member);

        //토큰 삭제
        ResetTokenRepository.delete(resetToken);
        successResponse.setStatus("SUCCESS");
       successResponse.setMessage("비밀번호가 성공적으로 변경되었습니다.");
        return ResponseEntity.ok().body(successResponse);
    }
}

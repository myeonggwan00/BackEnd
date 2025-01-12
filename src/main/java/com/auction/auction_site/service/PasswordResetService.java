package com.auction.auction_site.service;

import com.auction.auction_site.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PasswordResetService {

    @Autowired
    private MemberRepository memberRepository;

    /**
     *  이메일 확인
     */
    public findMemberByEmail


    /**
     * 메일 전송
     * - 비밀번호 재설정 url
     */
    public String sendPasswordResetMail(String email){

    }


    public String makeUUID(){
        return UUID.randomUUID().toString();
    }

}

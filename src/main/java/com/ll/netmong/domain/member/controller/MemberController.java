package com.ll.netmong.domain.member.controller;

import com.ll.netmong.domain.member.entity.Member;
import com.ll.netmong.domain.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/find")
    public Member findMember(){
        return memberService.findById(1L);
    }

    //db테스트용으로 잠시 get으로 해놓음
    @GetMapping("/create")
    public Member create(){
        return memberService.save();
    }
}

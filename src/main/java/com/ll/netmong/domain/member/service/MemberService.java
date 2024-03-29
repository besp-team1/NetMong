package com.ll.netmong.domain.member.service;

import com.ll.netmong.base.jwt.TokenDto;
import com.ll.netmong.base.jwt.TokenService;
import com.ll.netmong.domain.member.dto.EmailRequest;
import com.ll.netmong.domain.member.dto.JoinRequest;
import com.ll.netmong.domain.member.dto.LoginDto;
import com.ll.netmong.domain.member.dto.UsernameRequest;
import com.ll.netmong.domain.member.entity.AuthLevel;
import com.ll.netmong.domain.member.entity.Member;
import com.ll.netmong.domain.member.entity.ProviderTypeCode;
import com.ll.netmong.domain.member.exception.AlreadyUsedException;
import com.ll.netmong.domain.member.exception.NotMatchPasswordException;
import com.ll.netmong.domain.member.repository.MemberRepository;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.security.auth.login.AccountNotFoundException;
import java.util.Optional;

@Service
@Builder
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public Member findById(long id) {
        return memberRepository.findById(id).orElseThrow();
    }

    @Transactional
    public Member createMember(JoinRequest joinRequest) {
        Member member = Member.builder().username(joinRequest.getUsername())
                .password(joinRequest.getPassword())
                .realName(joinRequest.getRealname())
                .email(joinRequest.getEmail())
                .providerTypeCode(ProviderTypeCode.NETMONG)
                .authLevel(AuthLevel.MEMBER)
                .build();

        member.encryptPassword(passwordEncoder);

        return memberRepository.save(member);
    }

    public boolean isDuplicateUsername(UsernameRequest usernameRequest) {
        String username = usernameRequest.getUsername();
        return memberRepository.findByUsername(username).isPresent();
    }

    public boolean isDuplicateEmail(EmailRequest emailRequest) {
        return memberRepository.findByEmail(emailRequest.getEmail()).isPresent();
    }

    public TokenDto login(LoginDto loginDto) throws Exception {

        Member member = memberRepository.findByEmail(loginDto.getEmail())
                .orElseThrow(() -> new AccountNotFoundException("아이디/비밀번호가 잘못되었습니다."));

        boolean matches = passwordEncoder.matches(loginDto.getPassword(), member.getPassword());
        if (!matches) {
            throw new NotMatchPasswordException("잘못된 비밀번호입니다.");
        }

        return tokenService.provideTokenWithLoginDto(loginDto);
    }

    public Member findByEmail(String email) throws Exception {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new AccountNotFoundException("User not Found"));
    }


    public Member findByUsername(String username) throws Exception {
        return memberRepository.findByUsername(username)
                .orElseThrow(() -> new AccountNotFoundException("User not Found"));
    }

    @Transactional
    public String changePassword(Member member, String oldPassword, String newPassword, String repeatPassword) throws Exception {

        if (passwordEncoder.matches(oldPassword, member.getPassword()) && newPassword.equals(repeatPassword)) {
            member.changePassword(newPassword);
            member.encryptPassword(passwordEncoder);
        }
        return memberRepository.save(member).getUsername();
    }

    public Long countPostsByUsername(String username) {
        return memberRepository.countPostsByMemberUsername(username);
    }

    @Transactional
    public String changeUsername(Member member, String newUsername) {
        Optional<Member> optMember = memberRepository.findByUsername(newUsername);
        if (optMember.isPresent()) {
            throw new AlreadyUsedException("이미 사용중인 닉네임입니다.");
        }
        member.changeUsername(newUsername);
        return member.getUsername();
    }
}

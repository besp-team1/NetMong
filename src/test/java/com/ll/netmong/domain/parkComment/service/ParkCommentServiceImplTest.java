package com.ll.netmong.domain.parkComment.service;

import com.ll.netmong.domain.member.entity.Member;
import com.ll.netmong.domain.member.repository.MemberRepository;
import com.ll.netmong.domain.park.entity.Park;
import com.ll.netmong.domain.park.repository.ParkRepository;
import com.ll.netmong.domain.parkComment.dto.request.ParkCommentRequest;
import com.ll.netmong.domain.parkComment.dto.response.ParkCommentResponse;
import com.ll.netmong.domain.parkComment.entity.ParkComment;
import com.ll.netmong.domain.parkComment.repository.ParkCommentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;

@SpringBootTest
class ParkCommentServiceImplTest {

    @Autowired
    private ParkCommentServiceImpl parkCommentService;

    @MockBean
    private ParkRepository parkRepository;

    @MockBean
    private MemberRepository memberRepository;

    @MockBean
    private ParkCommentRepository parkCommentRepository;

    private Park park;
    private Member member;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        Long parkId = 1L;
        park = Park.builder().id(parkId).comments(new ArrayList<>()).build();

        String username = "testUser";
        userDetails = User.withUsername(username).password("testPassword").authorities("USER").build();
        member = Member.builder().username(username).build();

        when(parkRepository.findById(parkId)).thenReturn(Optional.of(park));
        when(memberRepository.findByUsername(username)).thenReturn(Optional.of(member));
    }

    @Test
    @DisplayName("addParkComment() 메서드가 주어진 공원 ID, 댓글 내용, 사용자 정보를 이용하여 새로운 댓글을 생성하고, 저장하는지 검증한다.")
    void testAddParkCommentExists() {
        // Given
        ParkCommentRequest request = new ParkCommentRequest();
        request.setContent("TestContent");

        ParkComment comment = ParkComment.builder()
                .park(park)
                .memberID(member)
                .username(userDetails.getUsername())
                .content(request.getContent())
                .isDeleted(false)
                .build();
        when(parkCommentRepository.save(any(ParkComment.class))).thenReturn(comment);

        // When
        ParkCommentResponse response = parkCommentService.addParkComment(park.getId(), request, userDetails);

        // Then
        assertNotNull(response);
        assertEquals(comment.getContent(), response.getContent());
        assertEquals(comment.getUsername(), response.getUsername());
        assertFalse(response.getIsDeleted());
    }

}
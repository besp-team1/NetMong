package com.ll.netmong.domain.parkComment.service;

import com.ll.netmong.domain.member.entity.Member;
import com.ll.netmong.domain.member.repository.MemberRepository;
import com.ll.netmong.domain.park.entity.Park;
import com.ll.netmong.domain.park.repository.ParkRepository;
import com.ll.netmong.domain.parkComment.dto.request.ParkCommentRequest;
import com.ll.netmong.domain.parkComment.dto.response.ParkCommentResponse;
import com.ll.netmong.domain.parkComment.entity.ParkComment;
import com.ll.netmong.domain.parkComment.repository.ParkCommentRepository;
import com.ll.netmong.domain.postComment.exception.DataNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParkCommentServiceImpl implements ParkCommentService {

    private final ParkCommentRepository parkCommentRepository;
    private final ParkRepository parkRepository;
    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public ParkCommentResponse addParkComment(Long parkId, ParkCommentRequest parkCommentRequest, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Park park = parkRepository.findWithOptimisticLockById(parkId)
                    .orElseThrow(() -> new DataNotFoundException("해당하는 공원을 찾을 수 없습니다."));

            Member member = memberRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new DataNotFoundException("사용자를 찾을 수 없습니다."));

            park.updatePetAllowed(parkCommentRequest.getPetAllowed());

            ParkComment comment = ParkComment.builder()
                    .park(park)
                    .memberID(member)
                    .username(member.getUsername())
                    .content(parkCommentRequest.getContent())
                    .isDeleted(false)
                    .build();
            park.addComment(comment);
            ParkComment savedComment = parkCommentRepository.save(comment);
            return savedComment.toResponse();
        } catch (OptimisticLockingFailureException e) {
            throw new OptimisticLockingFailureException("다른 사용자가 동시에 데이터를 수정하였습니다. 다시 시도해주세요.");
        }
    }

    @Override
    @Transactional
    public Page<ParkCommentResponse> getCommentsOfPark(Long parkId, Pageable pageable) {
        Page<ParkComment> comments = parkCommentRepository.findByParkIdAndIsDeletedFalse(parkId, pageable);
        return comments.map(ParkComment::toResponse);
    }

    @Override
    @Transactional
    public ParkCommentResponse updateComment(Long commentId, ParkCommentRequest updateRequest, @AuthenticationPrincipal UserDetails userDetails) {
        ParkComment comment = parkCommentRepository.findById(commentId)
                .orElseThrow(() -> new DataNotFoundException("해당하는 댓글을 찾을 수 없습니다."));

        checkCommentAuthor(comment, userDetails);
        comment.updateContent(updateRequest.getContent());
        ParkComment updatedComment = parkCommentRepository.save(comment);
        return updatedComment.toResponse();
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, @AuthenticationPrincipal UserDetails userDetails) {
        ParkComment comment = parkCommentRepository.findById(commentId)
                .orElseThrow(() -> new DataNotFoundException("해당 댓글이 없습니다. id: " + commentId));
        checkCommentAuthor(comment, userDetails);
        comment.markAsDeleted(true);
        parkCommentRepository.save(comment);
    }

    private void checkCommentAuthor(ParkComment comment, UserDetails userDetails) {
        Member member = memberRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new DataNotFoundException("해당하는 회원을 찾을 수 없습니다."));

        if (!comment.getUsername().equals(member.getUsername())) {
            throw new AccessDeniedException("댓글 작성자만 수정할 수 있습니다.");
        }
    }

}
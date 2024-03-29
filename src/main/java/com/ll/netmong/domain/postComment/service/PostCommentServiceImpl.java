package com.ll.netmong.domain.postComment.service;

import com.ll.netmong.domain.member.entity.Member;
import com.ll.netmong.domain.member.repository.MemberRepository;
import com.ll.netmong.domain.post.entity.Post;
import com.ll.netmong.domain.post.repository.PostRepository;
import com.ll.netmong.domain.postComment.dto.request.PostCommentRequest;
import com.ll.netmong.domain.postComment.dto.response.PostCommentResponse;
import com.ll.netmong.domain.postComment.entity.PostComment;
import com.ll.netmong.domain.postComment.exception.DataNotFoundException;
import com.ll.netmong.domain.postComment.repository.PostCommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostCommentServiceImpl implements PostCommentService {

    private final PostCommentRepository postCommentRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    public PostComment findById(Long id) {
        return postCommentRepository.findById(id).orElseThrow();
    }

    @Override
    @Transactional
    public PostCommentResponse addPostComment(Long postId, PostCommentRequest postCommentRequest, @AuthenticationPrincipal UserDetails userDetails) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new DataNotFoundException("해당하는 게시물을 찾을 수 없습니다."));

        Member member = memberRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new DataNotFoundException("사용자를 찾을 수 없습니다."));

        // 부모 댓글이 없는 경우 depth는 0으로 설정
        Integer depth = 0;

        PostComment comment = PostComment.builder()
                .post(post)
                .memberID(member)
                .username(member.getUsername())
                .content(postCommentRequest.getContent())
                .isDeleted(false)
                .isBlinded(false)
                .depth(depth)
                .build();
        post.addComment(comment);
        PostComment savedComment = postCommentRepository.save(comment);
        return PostCommentResponse.of(savedComment);
    }

    @Override
    @Transactional
    public PostCommentResponse updateComment(Long commentId, PostCommentRequest updateRequest, @AuthenticationPrincipal UserDetails userDetails) {
        PostComment comment = postCommentRepository.findById(commentId)
                .orElseThrow(() -> new DataNotFoundException("해당하는 댓글을 찾을 수 없습니다."));
        checkCommentAuthor(comment, userDetails);
        comment.updateContent(updateRequest.getContent());
        PostComment updatedComment = postCommentRepository.save(comment);
        return PostCommentResponse.of(updatedComment);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, @AuthenticationPrincipal UserDetails userDetails) {
        PostComment comment = postCommentRepository.findById(commentId)
                .orElseThrow(() -> new DataNotFoundException("해당 댓글이 없습니다. id: " + commentId));
        checkCommentAuthor(comment, userDetails);
        comment.markAsDeleted(true);
        postCommentRepository.save(comment);
    }

    @Override
    @Transactional
    public Page<PostCommentResponse> getCommentsOfPost(Long postId, Pageable pageable) {
        Page<PostComment> comments = postCommentRepository.findByPostIdAndParentCommentIsNull(postId, pageable);
        return comments.map(this::convertToResponse);
    }

    private PostCommentResponse convertToResponse(PostComment comment) {
        List<PostCommentResponse> childResponses = comment.getChildComments() != null ? comment.getChildComments().stream().map(this::convertToResponse).collect(Collectors.toList()) : new ArrayList<>();
        return new PostCommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getIsDeleted(),
                comment.getUsername(),
                comment.getParentComment() != null ? comment.getParentComment().getId() : null,
                childResponses,
                comment.getDepth()
                );
    }

    private void checkCommentAuthor(PostComment comment, UserDetails userDetails) {
        Member member = memberRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new DataNotFoundException("해당하는 회원을 찾을 수 없습니다."));

        if (!comment.getUsername().equals(member.getUsername())) {
            throw new AccessDeniedException("댓글 작성자만 수정할 수 있습니다.");
        }
    }

    @Override
    @Transactional
    public PostCommentResponse addReplyToComment(Long commentId, PostCommentRequest request, @AuthenticationPrincipal UserDetails userDetails) {
        PostComment parentComment = postCommentRepository.findById(commentId)
                .orElseThrow(() -> new DataNotFoundException("해당 댓글이 없습니다. id: " + commentId));
        Member member = memberRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new DataNotFoundException("해당하는 회원을 찾을 수 없습니다."));

        // 부모 댓글의 깊이를 기반으로 자식 댓글의 깊이 설정
        Integer childDepth = parentComment.getDepth() + 1;

        PostComment childComment = PostComment.builder()
                .post(parentComment.getPost())
                .memberID(member)
                .content(request.getContent())
                .isDeleted(false)
                .isBlinded(false)
                .username(member.getUsername())
                .depth(childDepth)
                .build();

        parentComment.addChildComment(childComment);
        PostComment savedChildComment = postCommentRepository.save(childComment);
        return convertToResponse(savedChildComment);
    }

    @Override
    @Transactional
    public PostCommentResponse updateReply(Long replyId, PostCommentRequest request) {
        PostComment reply = postCommentRepository.findById(replyId)
                .orElseThrow(() -> new DataNotFoundException("해당 대댓글이 없습니다. id: " + replyId));
        reply.updateContent(request.getContent());
        PostComment updatedReply = postCommentRepository.save(reply);
        return convertToResponse(updatedReply);
    }

    @Override
    public PostComment findByCommentId(Long commentId) {
        return postCommentRepository.findById(commentId)
                .orElseThrow(() -> new DataNotFoundException("해당하는 댓글을 찾을 수 없습니다."));
    }

    private void addChildComment(PostComment parentComment, PostComment childComment) {
        childComment.setParentComment(parentComment);
        // 부모 댓글의 깊이에 1을 더하여 자식 댓글의 깊이를 설정
        childComment.setDepth(parentComment.getDepth() + 1);
        parentComment.addChildComment(childComment);
    }
}

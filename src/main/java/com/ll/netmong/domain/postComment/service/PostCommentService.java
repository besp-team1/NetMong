package com.ll.netmong.domain.postComment.service;

import com.ll.netmong.domain.postComment.dto.request.PostCommentRequest;
import com.ll.netmong.domain.postComment.dto.response.PostCommentResponse;
import com.ll.netmong.domain.postComment.entity.PostComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;

public interface PostCommentService {

    PostCommentResponse addPostComment(Long postId, PostCommentRequest postCommentRequest, UserDetails userDetails);

    PostCommentResponse updateComment(Long commentId, PostCommentRequest request, UserDetails userDetails);

    void deleteComment(Long commentId, UserDetails userDetails);

    Page<PostCommentResponse> getCommentsOfPost(Long postId, Pageable pageable);

    PostCommentResponse addReplyToComment(Long commentId, PostCommentRequest request, UserDetails userDetails);

    PostCommentResponse updateReply(Long replyId, PostCommentRequest request);

    PostComment findByCommentId(Long commentId);
}

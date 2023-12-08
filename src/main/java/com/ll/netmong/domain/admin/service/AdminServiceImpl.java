package com.ll.netmong.domain.admin.service;

import com.ll.netmong.domain.admin.dto.reponse.AdminReportPostCommentResponse;
import com.ll.netmong.domain.postComment.entity.PostComment;
import com.ll.netmong.domain.postComment.repository.PostCommentRepository;
import com.ll.netmong.domain.reportPost.dto.response.ReportPostResponse;
import com.ll.netmong.domain.reportPost.entity.ReportPost;
import com.ll.netmong.domain.reportPost.repository.ReportPostRepository;
import com.ll.netmong.domain.reportPostComment.entity.ReportPostComment;
import com.ll.netmong.domain.reportPostComment.repository.ReportPostCommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminServiceImpl implements AdminService {

    private final ReportPostRepository reportPostRepository;
    private final PostCommentRepository postCommentRepository;
    private final ReportPostCommentRepository reportPostCommentRepository;

    @Override
    public List<ReportPostResponse> getAllReportPosts() {
        List<ReportPost> reports = reportPostRepository.findAll();
        return reports.stream()
                .map(ReportPost::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<AdminReportPostCommentResponse> getAllReportedComments() {
        List<ReportPostComment> reportedComments = reportPostCommentRepository.findAll();

        Map<Long, AdminReportPostCommentResponse> commentsMap = new HashMap<>();
        for (ReportPostComment report : reportedComments) {
            AdminReportPostCommentResponse response = AdminReportPostCommentResponse.of(report);
            commentsMap.put(response.getId(), response);
        }

        return new ArrayList<>(commentsMap.values());
    }

}

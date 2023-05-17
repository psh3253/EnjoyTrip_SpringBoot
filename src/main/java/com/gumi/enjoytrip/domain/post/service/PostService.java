package com.gumi.enjoytrip.domain.post.service;

import com.gumi.enjoytrip.domain.post.dto.*;
import com.gumi.enjoytrip.domain.post.entity.Comment;
import com.gumi.enjoytrip.domain.post.entity.LikePost;
import com.gumi.enjoytrip.domain.post.entity.Post;
import com.gumi.enjoytrip.domain.post.exception.CommentNotFoundException;
import com.gumi.enjoytrip.domain.post.exception.InvalidUserException;
import com.gumi.enjoytrip.domain.post.exception.PostNotFoundException;
import com.gumi.enjoytrip.domain.post.repository.CommentRepository;
import com.gumi.enjoytrip.domain.post.repository.LikePostRepository;
import com.gumi.enjoytrip.domain.post.repository.PostRepository;
import com.gumi.enjoytrip.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final LikePostRepository likePostRepository;
    private final CommentRepository commentRepository;

    @Transactional(readOnly = true)
    public List<PostListDto> getPostList(int page, String keyword) {
        Pageable pageable = PageRequest.of(page - 1, 15);
        return postRepository.findAllByTitleContainingIgnoreCaseOrderByIsNoticeDescIdDesc(keyword, pageable)
                .stream()
                .map(post -> toPostListDto(post, likePostRepository.countByPostId(post.getId())))
                .toList();
    }


    @Transactional(readOnly = true)
    public int getPageCount() {
        return (int) Math.ceil((double) postRepository.count() / 15);
    }

    @Transactional
    public long createPost(PostCreateDto postCreateDto, User user) {
        return postRepository.save(postCreateDto.toEntity(user)).getId();
    }

    @Transactional
    public PostDto getPost(long id, User user) {
        Post post = postRepository.findById(id).orElseThrow(() -> new PostNotFoundException("존재하지 않는 게시글입니다."));
        postRepository.increaseViews(id);
        boolean isLiked = likePostRepository.countByPostIdAndUserId(id, user.getId()) != 0;
        int likeCount = likePostRepository.countByPostId(id);
        return toPostDto(post, isLiked, likeCount);
    }

    @Transactional
    public long updatePost(long id, PostUpdateDto postUpdateDto, User user) {
        log.info("updatePost: {}", postUpdateDto);
        Post post = postRepository.findById(id).orElseThrow(() -> new PostNotFoundException("존재하지 않는 게시글입니다."));
        if (!Objects.equals(post.getUser().getId(), user.getId())) {
            throw new InvalidUserException("작성자만 수정할 수 있습니다.");
        }
        return postRepository.save(post.update(postUpdateDto.toEntity(post.getIsNotice()))).getId();
    }

    @Transactional
    public void deletePost(long id, User user) {
        Post post = postRepository.findById(id).orElseThrow(() -> new PostNotFoundException("존재하지 않는 게시글입니다."));
        if (!Objects.equals(post.getUser().getId(), user.getId())) {
            throw new InvalidUserException("작성자만 삭제할 수 있습니다.");
        }
        postRepository.deleteById(id);
    }

    @Transactional
    public void togglePostLike(long id, User user) {
        Post post = postRepository.findById(id).orElseThrow(() -> new PostNotFoundException("존재하지 않는 게시글입니다."));
        if (likePostRepository.countByPostIdAndUserId(id, user.getId()) == 0) {
            likePostRepository.save(LikePost.builder().post(post).user(user).build());
        } else {
            likePostRepository.deleteByPostIdAndUserId(id, user.getId());
        }
    }

    @Transactional
    public void togglePostNotice(long id, User user) {
        Post post = postRepository.findById(id).orElseThrow(() -> new PostNotFoundException("존재하지 않는 게시글입니다."));
        if (user.getRole().equals("ROLE_ADMIN")) {
            throw new InvalidUserException("관리자만 공지사항을 설정 및 해제할 수 있습니다.");
        }
        postRepository.save(post.update(Post.builder().isNotice(!post.getIsNotice()).build()));
    }

    @Transactional(readOnly = true)
    public List<LikeUserListDto> getLikeUserList(long id) {
        postRepository.findById(id).orElseThrow(() -> new PostNotFoundException("존재하지 않는 게시글입니다."));
        return likePostRepository.findAllByPostId(id).stream()
                .map(this::toLikeUserListDto)
                .toList();
    }

    public List<CommentListDto> getCommentList(long id) {
        postRepository.findById(id).orElseThrow(() -> new PostNotFoundException("존재하지 않는 게시글입니다."));
        return commentRepository.findAllByPostId(id).stream()
                .map(comment -> toCommentList(comment))
                .toList();
    }

    public void createComment(long id, User user, CommentCreateDto commentCreateDto) {
        Post post = postRepository.findById(id).orElseThrow(() -> new PostNotFoundException("존재하지 않는 게시글입니다."));
        commentRepository.save(commentCreateDto.toEntity(post, user));
    }

    public void deleteComment(long id, User user) {
        Comment comment = commentRepository.findById(id).orElseThrow(() -> new CommentNotFoundException("존재하지 않는 댓글 입니다."));
        if(comment.getUser().getId() != user.getId()) {
            throw new InvalidUserException("작성자만 삭제할 수 있습니다.");
        }
        commentRepository.deleteById(id);
    }

    public PostListDto toPostListDto(Post post, int likeCount) {
        return new PostListDto(
                post.getId(),
                post.getTitle(),
                post.getViews(),
                post.getIsNotice(),
                likeCount,
                post.getUser().getId(),
                post.getUser().getNickname(),
                post.getCreatedAt()
        );
    }

    public PostDto toPostDto(Post post, boolean isLiked, int likeCount) {
        return new PostDto(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getViews(),
                isLiked,
                post.getIsNotice(),
                likeCount,
                post.getUser().getId(),
                post.getUser().getNickname(),
                post.getCreatedAt()
        );
    }

    public LikeUserListDto toLikeUserListDto(LikePost likePost) {
        return new LikeUserListDto(
                likePost.getId(),
                likePost.getUser().getId(),
                likePost.getUser().getNickname(),
                likePost.getUser().getImageFileName(),
                likePost.getCreatedAt()
        );
    }

    private CommentListDto toCommentList(Comment comment) {
        return new CommentListDto(
                comment.getId(),
                comment.getUser().getId(),
                comment.getUser().getNickname(),
                comment.getContent(),
                comment.getCreatedAt()
        );
    }
}

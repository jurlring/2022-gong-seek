package com.woowacourse.gongseek.article.infra.repository;

import static com.querydsl.core.group.GroupBy.groupBy;
import static com.querydsl.core.types.ExpressionUtils.count;
import static com.woowacourse.gongseek.article.domain.QArticle.article;
import static com.woowacourse.gongseek.article.domain.articletag.QArticleTag.articleTag;
import static com.woowacourse.gongseek.comment.domain.QComment.comment;
import static com.woowacourse.gongseek.like.domain.QLike.like;
import static com.woowacourse.gongseek.member.domain.QMember.member;
import static com.woowacourse.gongseek.tag.domain.QTag.tag;

import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.woowacourse.gongseek.article.domain.Category;
import com.woowacourse.gongseek.article.domain.repository.PagingArticleRepository;
import com.woowacourse.gongseek.article.domain.repository.dto.ArticlePreviewDto;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class PagingArticleRepositoryImpl implements PagingArticleRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Slice<ArticlePreviewDto> findAllByPage(Long cursorId, Long views, String category, String sortType,
                                                  Long memberId,
                                                  Pageable pageable) {
        JPAQuery<ArticlePreviewDto> query = queryFactory
                .select(
                        Projections.constructor(
                                ArticlePreviewDto.class,
                                article.id,
                                article.title.value,
                                article.member,
                                article.content.value,
                                article.category,
                                article.isAnonymous,
                                article.views.value,
                                count(comment),
                                article.likeCount.value,
                                article.createdAt
                        )
                )
                .from(article)
                .join(article.member, member)
                .leftJoin(comment).on(article.id.eq(comment.article.id))
                .where(
                        cursorIdAndCursorViews(cursorId, views, sortType),
                        categoryEquals(category)
                )
                .groupBy(article.id)
                .limit(pageable.getPageSize() + 1);
        List<ArticlePreviewDto> fetch = sort(sortType, query);
        setTagNameAndIsLike(fetch, memberId);

        return convertToSliceFromArticle(fetch, pageable);
    }

    private BooleanExpression cursorIdAndCursorViews(Long cursorId, Long cursorViews, String sortType) {
        if (sortType.equals("views")) {
            if (cursorId == null || cursorViews == null) {
                return null;
            }

            return article.views.value.eq(cursorViews)
                    .and(article.id.lt(cursorId))
                    .or(article.views.value.lt(cursorViews));
        }

        return isOverArticleId(cursorId);
    }

    private BooleanExpression isOverArticleId(Long cursorId) {
        return cursorId == null ? null : article.id.lt(cursorId);
    }

    private BooleanExpression categoryEquals(String category) {
        return "all".equals(category) ? null : article.category.eq(Category.from(category));
    }

    private List<ArticlePreviewDto> sort(String sortType, JPAQuery<ArticlePreviewDto> query) {
        if (sortType.equals("views")) {
            return query.orderBy(article.views.value.desc(), article.id.desc()).fetch();
        }
        return query.orderBy(article.id.desc()).fetch();
    }

    private SliceImpl<ArticlePreviewDto> convertToSliceFromArticle(List<ArticlePreviewDto> fetch, Pageable pageable) {
        boolean hasNext = false;

        if (fetch.size() == pageable.getPageSize() + 1) {
            fetch.remove(pageable.getPageSize());
            hasNext = true;
        }
        return new SliceImpl<>(fetch, pageable, hasNext);
    }

    private void setTagNameAndIsLike(List<ArticlePreviewDto> fetch, Long memberId) {
        List<Long> articleIds = toArticleIds(fetch);
        Map<Long, List<String>> tagNameMap = findTagNameMap(articleIds);
        List<Long> findLikeArticleIds = findLikeArticleIds(articleIds, memberId);
        fetch.forEach(o -> {
            o.setTagName(tagNameMap.get(o.getId()));
            o.setIsLike(findLikeArticleIds.contains(o.getId()));
        });
    }

    private List<Long> toArticleIds(List<ArticlePreviewDto> result) {
        return result.stream()
                .map(ArticlePreviewDto::getId)
                .collect(Collectors.toList());
    }

    private Map<Long, List<String>> findTagNameMap(List<Long> articleIds) {
        return queryFactory
                .from(article)
                .leftJoin(article.articleTags.value, articleTag)
                .leftJoin(articleTag.tag, tag)
                .where(article.id.in(articleIds))
                .transform(groupBy(article.id).as(GroupBy.list(tag.name)));
    }

    private List<Long> findLikeArticleIds(List<Long> articleIds, Long memberId) {
        return queryFactory
                .select(like.article.id)
                .from(like)
                .where(
                        like.article.id.in(articleIds),
                        like.member.id.eq(memberId)
                )
                .fetch();
    }

    @Override
    public Slice<ArticlePreviewDto> findAllByLikes(Long cursorId, Long cursorLike, String category, Long memberId,
                                                   Pageable pageable) {
        List<ArticlePreviewDto> fetch = queryFactory
                .select(
                        Projections.constructor(
                                ArticlePreviewDto.class,
                                article.id,
                                article.title.value,
                                article.member,
                                article.content.value,
                                article.category,
                                article.isAnonymous,
                                article.views.value,
                                count(comment),
                                article.likeCount.value,
                                article.createdAt
                        )
                )
                .from(article)
                .join(article.member, member)
                .leftJoin(comment).on(article.id.eq(comment.article.id))
                .where(
                        cursorIdAndLikes(cursorId, cursorLike),
                        categoryEquals(category)
                )
                .groupBy(article.id)
                .limit(pageable.getPageSize() + 1)
                .orderBy(article.likeCount.value.desc(), article.id.desc())
                .fetch();
        setTagNameAndIsLike(fetch, memberId);

        return convertToSliceFromArticle(fetch, pageable);
    }

    private BooleanExpression cursorIdAndLikes(Long cursorId, Long likes) {
        if (cursorId == null || likes == null) {
            return null;
        }
        return article.likeCount.value.eq(likes)
                .and(article.id.lt(cursorId))
                .or(article.likeCount.value.lt(likes));
    }

    @Override
    public Slice<ArticlePreviewDto> searchByContainingText(Long cursorId, String searchText, Long memberId,
                                                           Pageable pageable) {
        List<ArticlePreviewDto> fetch = queryFactory
                .select(
                        Projections.constructor(
                                ArticlePreviewDto.class,
                                article.id,
                                article.title.value,
                                article.member,
                                article.content.value,
                                article.category,
                                article.isAnonymous,
                                article.views.value,
                                count(comment),
                                article.likeCount.value,
                                article.createdAt
                        )
                )
                .from(article)
                .join(article.member, member)
                .leftJoin(comment).on(article.id.eq(comment.article.id))
                .where(
                        containsTitleOrContent(searchText),
                        isOverArticleId(cursorId)
                )
                .groupBy(article.id)
                .limit(pageable.getPageSize() + 1)
                .orderBy(article.id.desc())
                .fetch();
        setTagNameAndIsLike(fetch, memberId);

        return convertToSliceFromArticle(fetch, pageable);
    }

    private BooleanExpression containsTitleOrContent(String searchText) {
        return article.title.value.contains(searchText)
                .or(article.content.value.contains(searchText));
    }

    @Override
    public Slice<ArticlePreviewDto> searchByAuthor(Long cursorId, String author, Long memberId, Pageable pageable) {
        List<ArticlePreviewDto> fetch = queryFactory
                .select(
                        Projections.constructor(
                                ArticlePreviewDto.class,
                                article.id,
                                article.title.value,
                                article.member,
                                article.content.value,
                                article.category,
                                article.isAnonymous,
                                article.views.value,
                                count(comment),
                                article.likeCount.value,
                                article.createdAt
                        )
                )
                .from(article)
                .join(article.member, member)
                .leftJoin(comment).on(article.id.eq(comment.article.id))
                .where(
                        article.member.name.value.eq(author),
                        article.isAnonymous.eq(false),
                        isOverArticleId(cursorId)
                )
                .groupBy(article.id)
                .limit(pageable.getPageSize() + 1)
                .orderBy(article.id.desc())
                .fetch();
        setTagNameAndIsLike(fetch, memberId);

        return convertToSliceFromArticle(fetch, pageable);
    }

    @Override
    public Slice<ArticlePreviewDto> searchByTag(Long cursorId, Long memberId, List<String> tagNames,
                                                Pageable pageable) {
        List<ArticlePreviewDto> fetch = queryFactory
                .select(
                        Projections.constructor(
                                ArticlePreviewDto.class,
                                article.id,
                                article.title.value,
                                article.member,
                                article.content.value,
                                article.category,
                                article.isAnonymous,
                                article.views.value,
                                article.views.value,
                                article.likeCount.value,
                                article.createdAt
                        )
                )
                .distinct()
                .from(articleTag)
                .join(articleTag.article, article)
                .join(article.member, member)
                .join(articleTag.tag, tag)
                .leftJoin(comment).on(article.id.eq(comment.article.id))
                .where(
                        articleTag.tag.name.in(getUpperTagNames(tagNames)),
                        isOverArticleId(cursorId)
                )
                .groupBy(article.id)
                .limit(pageable.getPageSize() + 1)
                .orderBy(articleTag.article.id.desc())
                .fetch();
        setTagNameAndIsLike(fetch, memberId);

        return convertToSliceFromArticle(fetch, pageable);
    }

    private List<String> getUpperTagNames(List<String> tagNames) {
        return tagNames.stream()
                .map(String::toUpperCase)
                .collect(Collectors.toList());
    }
}

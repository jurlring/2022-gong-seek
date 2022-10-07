import React, { Suspense, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

import ArticleItem from '@/components/common/ArticleItem/ArticleItem';
import EmptyMessage from '@/components/common/EmptyMessage/EmptyMessage';
import Loading from '@/components/common/Loading/Loading';
import useGetSearch from '@/hooks/search/useGetSearch';
import * as S from '@/pages/Search/SearchResult/SearchResult.styles';

const ResponsiveInfiniteCardList = React.lazy(
	() => import('@/components/common/ResponsiveInfiniteCardList/ResponsiveInfiniteCardList'),
);

const SearchResult = ({ target, searchIndex }: { target: string; searchIndex: string }) => {
	const { data, isLoading, isIdle, refetch, fetchNextPage } = useGetSearch({ target, searchIndex });
	const navigate = useNavigate();

	useEffect(() => {
		refetch();
	}, [target, searchIndex]);

	if (isLoading || isIdle) {
		return <Loading />;
	}

	return (
		<S.Container>
			<S.Title>검색 결과</S.Title>
			<Suspense fallback={<Loading />}>
				{data && data.pages[0].articles.length >= 1 ? (
					<ResponsiveInfiniteCardList
						hasNext={data.pages[data.pages.length - 1].hasNext}
						fetchNextPage={fetchNextPage}
					>
						<>
							{data.pages.map(({ articles }) =>
								articles.map((article) => (
									<ArticleItem
										key={article.id}
										article={article}
										onClick={() => {
											navigate(`/articles/${article.category}/${article.id}`);
										}}
									/>
								)),
							)}
						</>
					</ResponsiveInfiniteCardList>
				) : (
					<EmptyMessage>검색 결과가 존재하지 않습니다</EmptyMessage>
				)}
			</Suspense>
		</S.Container>
	);
};

export default SearchResult;

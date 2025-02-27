import EmptyMessage from '@/components/@common/EmptyMessage/EmptyMessage';
import Loading from '@/components/@common/Loading/Loading';
import * as S from '@/components/user/UserCommentBox/UserCommentBox.styles';
import UserCommentItem from '@/components/user/UserCommentItem/UserCommentItem';
import useGetUserComments from '@/hooks/user/useGetUserComments';

const UserCommentBox = () => {
	const {
		data: comments,
		isSuccess: isCommentsSuccess,
		isLoading: isCommentsLoading,
	} = useGetUserComments();

	if (isCommentsLoading) {
		return <Loading />;
	}

	return (
		<>
			{isCommentsSuccess ? (
				<S.Container>
					{comments ? (
						comments.comments.map((comment) => (
							<UserCommentItem key={comment.id} comment={comment} />
						))
					) : (
						<EmptyMessage>작성하신 댓글이 없습니다</EmptyMessage>
					)}
				</S.Container>
			) : (
				<div>정보를 가져오는데 실패하였습니다</div>
			)}
		</>
	);
};

export default UserCommentBox;

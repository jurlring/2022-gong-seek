import axios from 'axios';

import { ACCESSTOKEN_KEY } from '@/constants';
import { HOME_URL } from '@/constants/apiUrl';
import { CommentType } from '@/types/commentResponse';

export const postComments = ({
	content,
	id,
	isAnonymous,
}: {
	content: string;
	id: string;
	isAnonymous: boolean;
}) => {
	const accessToken = localStorage.getItem(ACCESSTOKEN_KEY);
	return axios.post(
		`${HOME_URL}/api/articles/${id}/comments`,
		{ content, isAnonymous },
		{
			headers: {
				'Access-Control-Allow-Origin': '*',
				Authorization: `Bearer ${accessToken}`,
			},
		},
	);
};

export const getComments = async (id: string) => {
	const accessToken = localStorage.getItem(ACCESSTOKEN_KEY);

	const { data } = await axios.get<{ comments: CommentType[] }>(
		`${HOME_URL}/api/articles/${id}/comments`,
		{
			headers: {
				'Access-Control-Allow-Origin': '*',
				Authorization: `Bearer ${accessToken}`,
			},
		},
	);
	return data;
};

export const putComments = ({ content, commentId }: { content: string; commentId: string }) => {
	const accessToken = localStorage.getItem(ACCESSTOKEN_KEY);

	return axios.put(
		`${HOME_URL}/api/articles/comments/${commentId}`,
		{ content },
		{
			headers: {
				'Access-Control-Allow-Origin': '*',
				Authorization: `Bearer ${accessToken}`,
			},
		},
	);
};

export const deleteComments = ({ commentId }: { commentId: string }) => {
	const accessToken = localStorage.getItem(ACCESSTOKEN_KEY);

	return axios.delete(`${HOME_URL}/api/articles/comments/${commentId}`, {
		headers: {
			'Access-Control-Allow-Origin': '*',
			Authorization: `Bearer ${accessToken}`,
		},
	});
};

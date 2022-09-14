import React, { useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';

import Input from '@/components/common/Input/Input';
import { URL } from '@/constants/url';
import AddedOption from '@/pages/VoteGenerator/AddedOption/AddedOption';
import * as S from '@/pages/VoteGenerator/index.styles';

const VoteGenerator = () => {
	const [options, setOptions] = useState<string[]>([]);
	const [input, setInput] = useState('');
	const { articleId } = useParams();
	const navigate = useNavigate();

	const onSubmitAddOption = (e: React.FormEvent<HTMLFormElement>) => {
		e.preventDefault();
		if (input.length === 0) {
			return;
		}
		setOptions((prevoptions) => prevoptions.concat(input));
		setInput('');
	};

	const onSubmitVoteForm = (e: React.FormEvent<HTMLFormElement>) => {
		e.preventDefault();

		navigate(URL.VOTE_DEADLINE_GENERATOR, { state: { articleId, items: options } });
	};

	const onClickDeleteOptionButton = (id: number) => {
		const filteredOptions = options.filter((_, idx) => idx !== id);
		setOptions(filteredOptions);
	};

	return (
		<S.Container>
			<S.AddOptionForm onSubmit={onSubmitAddOption}>
				<S.OptionInputBox>
					<Input
						width="70%"
						type="text"
						value={input}
						onChange={(e: React.ChangeEvent<HTMLInputElement>) => {
							setInput(e.target.value);
						}}
					/>
					<S.AddButtonWrapper>
						<S.AddButton />
					</S.AddButtonWrapper>
				</S.OptionInputBox>
			</S.AddOptionForm>
			<S.ContentForm onSubmit={onSubmitVoteForm}>
				<S.RegisteredOptionTitle>등록된 항목</S.RegisteredOptionTitle>

				<S.Content>
					{options.map((option, idx) => (
						<AddedOption key={idx} onClick={() => onClickDeleteOptionButton(idx)}>
							{option}
						</AddedOption>
					))}
				</S.Content>
				<S.SubmitButton>등록하기</S.SubmitButton>
			</S.ContentForm>
		</S.Container>
	);
};

export default VoteGenerator;

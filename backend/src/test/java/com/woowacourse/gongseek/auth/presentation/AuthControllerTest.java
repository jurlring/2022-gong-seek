package com.woowacourse.gongseek.auth.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.woowacourse.gongseek.auth.presentation.dto.OAuthCodeRequest;
import com.woowacourse.gongseek.auth.presentation.dto.TokenResponse;
import com.woowacourse.gongseek.support.ControllerTest;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

@DisplayName("로그인 문서화")
class AuthControllerTest extends ControllerTest {

    @Test
    void 로그인_URL_조회_API_문서화() throws Exception {
        given(oAuthClient.getRedirectUrl()).willReturn("login url");
        mockMvc.perform(get("/api/auth/github"))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("login-url",
                                responseFields(
                                        fieldWithPath("url").type(JsonFieldType.STRING).description("로그인 요청 URL")
                                )
                        )
                );
    }

    @Test
    void 로그인_API_문서화() throws Exception {
        OAuthCodeRequest request = new OAuthCodeRequest("code");
        given(authService.generateToken(any())).willReturn(
                TokenResponse.builder()
                        .refreshToken(UUID.randomUUID())
                        .accessToken("accessToken")
                        .build()
        );
        ResultActions results = mockMvc.perform(post("/api/auth/login")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8"));

        results.andExpect(status().isOk())
                .andDo(print())
                .andDo(document("login-token",
                                responseHeaders(
                                        headerWithName(HttpHeaders.SET_COOKIE).description("리프레시 토큰")
                                ),
                                requestFields(
                                        fieldWithPath("code").type(JsonFieldType.STRING).description("사용자 인가코드")
                                ),
                                responseFields(
                                        fieldWithPath("accessToken").type(JsonFieldType.STRING).description("로그인 엑세스 토큰")
                                )
                        )
                );
    }

    @Test
    void 토큰_재발급_API_문서화() throws Exception {
        UUID refreshToken = UUID.randomUUID();

        doNothing().when(jwtTokenProvider).isValidAccessTokenWithTimeOut(any());

        given(authService.renewToken(any())).willReturn(
                TokenResponse.builder()
                        .refreshToken(refreshToken)
                        .accessToken("accessToken")
                        .build()
        );

        ResultActions results = mockMvc.perform(get("/api/auth/refresh")
                .header(HttpHeaders.AUTHORIZATION, "Bearer accessToken")
                .header(HttpHeaders.COOKIE, refreshToken)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8"));

        results.andExpect(status().isOk())
                .andDo(print())
                .andDo(document("renew-token",
                                requestHeaders(
                                        headerWithName(HttpHeaders.COOKIE).description("기존의 리프레시 토큰")
                                ), requestHeaders(
                                        headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer + 토큰")
                                ),
                                responseHeaders(
                                        headerWithName(HttpHeaders.SET_COOKIE).description("갱신된 리프레시 토큰")
                                ),
                                responseFields(
                                        fieldWithPath("accessToken").type(JsonFieldType.STRING).description("갱신된 엑세스 토큰")
                                )
                        )
                );
    }

    @Test
    void 로그아웃_API_문서화() throws Exception {

        ResultActions results = mockMvc.perform(delete("/api/auth/logout")
                .header(HttpHeaders.COOKIE, "refreshToken")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8"));

        results.andExpect(status().isNoContent())
                .andDo(print())
                .andDo(document("logout",
                                requestHeaders(
                                        headerWithName(HttpHeaders.COOKIE).description("기존의 리프레시 토큰")
                                ),
                                responseHeaders(
                                        headerWithName(HttpHeaders.SET_COOKIE).description("MAX_AGE가 0으로 만료된 토큰")
                                )
                        )
                );
    }
}

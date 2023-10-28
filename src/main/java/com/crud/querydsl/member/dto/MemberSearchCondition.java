package com.crud.querydsl.member.dto;

import lombok.Data;

@Data // 검색 조건 dto
public class MemberSearchCondition {

    private String username;
    private String teamName;
    private Integer ageGoe; // 나이가 크거나 같을 때
    private Integer ageLoe; // 나이가 적거나 같을 때
}

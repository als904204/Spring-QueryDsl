package com.crud.querydsl.domain.member;

import static com.crud.querydsl.domain.member.entity.QMember.member;
import static com.crud.querydsl.domain.team.entity.QTeam.team;
import static org.assertj.core.api.Assertions.assertThat;

import com.crud.querydsl.domain.member.dto.MemberSearchCondition;
import com.crud.querydsl.domain.member.dto.MemberTeamDto;
import com.crud.querydsl.domain.member.dto.QMemberTeamDto;
import com.crud.querydsl.domain.member.entity.Member;
import com.crud.querydsl.domain.team.entity.Team;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@SpringBootTest
@Transactional
class MemberJpaRepositoryTest {

    @Autowired
    EntityManager em;
    JPAQueryFactory queryFactory;
    @Autowired
    MemberJpaRepository memberJpaRepository;


    @BeforeEach
    public void before() {
        queryFactory = new JPAQueryFactory(em);
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);

        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

    }
    @Test
    public void basicTest() {
        Member member = new Member("member1", 10);
        memberJpaRepository.save(member);


        Member member4 = new Member("member4", 40);
        memberJpaRepository.save(member);
        List<Member> result1 = memberJpaRepository.finAll_QueryDsl();
        assertThat(result1).containsExactly(member); // 컬렉션에 담겨 있는지

        List<Member> result2 = memberJpaRepository.findByUsername_QueryDsl("member1");
        assertThat(result2).containsExactly(member);
    }

    @Test
    public void searchTest() {
        MemberSearchCondition condition = new MemberSearchCondition();
        condition.setAgeGoe(35);
        condition.setAgeLoe(40);
        condition.setTeamName("teamB");

        List<MemberTeamDto> result = memberJpaRepository.searchByWhere(condition);

        assertThat(result).extracting("username").containsExactly("member4");
    }




}
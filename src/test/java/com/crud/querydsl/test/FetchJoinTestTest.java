package com.crud.querydsl.test;

import static com.crud.querydsl.domain.member.entity.QMember.member;
import static org.assertj.core.api.Assertions.assertThat;
import static com.crud.querydsl.domain.team.entity.QTeam.team;

import com.crud.querydsl.member.entity.Member;
import com.crud.querydsl.team.entity.Team;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
@SpringBootTest
@Transactional
class FetchJoinTestTest {

    @Autowired
    EntityManager em;
    JPAQueryFactory queryFactory;

    @BeforeEach
    public void before() {
        queryFactory = new JPAQueryFactory(em);
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);

        Member member3 = new Member("member3", 20, teamB);
        Member member4 = new Member("member4", 20, teamB);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

    }

    @PersistenceUnit
    EntityManagerFactory emf;
    @DisplayName("Lazy 전략으로 인해 getTeam() 하지않는 이상 Team 을 조회하지 않는다")
    @Test
    public void fetchJoinNo() {
        em.flush();
        em.clear();

        Member member1 = queryFactory
            .selectFrom(member)
            .where(member.username.eq("member1"))
            .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(member1.getTeam());
        assertThat(loaded).as("Fetch 조인 미적용").isFalse();

    }

    @DisplayName("Lazy 조인 적용 후 Team 호출")
    @Test
    public void fetchJoinYES() {
        em.flush();
        em.clear();

        Member member1 = queryFactory
            .selectFrom(member)
            .join(member.team, team).fetchJoin()
            .where(member.username.eq("member1"))
            .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(member1.getTeam());
        assertThat(loaded).as("Fetch 조인 적용").isTrue();

    }

}
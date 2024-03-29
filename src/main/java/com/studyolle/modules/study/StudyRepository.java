package com.studyolle.modules.study;

import com.studyolle.modules.account.Account;
import com.studyolle.modules.tag.Tag;
import com.studyolle.modules.zone.Zone;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;


@Transactional(readOnly = true)
public interface StudyRepository extends JpaRepository<Study, Long> ,StudyRepositoryExtension{

    boolean existsByPath(String path);

//    @EntityGraph(value = "Study.withAll", type = EntityGraph.EntityGraphType.LOAD)
    @EntityGraph(attributePaths = {"tags","zones","managers","members"})
    Study findByPath(String path);

//    @EntityGraph(value = "Study.withTagsAndManagers", type = EntityGraph.EntityGraphType.FETCH)
    @EntityGraph(attributePaths = {"tags", "managers"})
    Study findStudyWithTagsByPath(String path);

//    @EntityGraph(value = "Study.withZonesAndManagers", type = EntityGraph.EntityGraphType.FETCH)
    @EntityGraph(attributePaths = {"zones", "managers"})
    Study findStudyWithZonesByPath(String path);

//    @EntityGraph(value = "Study.withManagers", type = EntityGraph.EntityGraphType.FETCH)
    @EntityGraph(attributePaths = "managers")
    Study findStudyWithManagersByPath(String path);

//    @EntityGraph(value = "Study.withMembers", type = EntityGraph.EntityGraphType.FETCH)
    @EntityGraph(attributePaths = "members")
    Study findStudyWithMembersByPath(String path);

    Study findStudyOnlyByPath(String path);

//    @EntityGraph(value = "Study.withZonesAndTags", type = EntityGraph.EntityGraphType.FETCH)
    @EntityGraph(attributePaths = {"zones","tags"})
    Study findStudyZonesAndTagsById(Long id);

    @EntityGraph(attributePaths = {"members", "managers"})
    Study findStudyWithManagersAndMembersById(Long id);

    List<Study> findFirst9ByPublishedAndClosedOrderByPublishedDateTimeDesc(boolean published, boolean closed);

    List<Study> findFirst5ByManagersContainingAndClosedOrderByPublishedDateTimeDesc(Account account, boolean closed);

    List<Study> findFirst5ByMembersContainingAndClosedOrderByPublishedDateTimeDesc(Account account, boolean closed);
}

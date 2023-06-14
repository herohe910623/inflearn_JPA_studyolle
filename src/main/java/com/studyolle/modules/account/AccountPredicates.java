package com.studyolle.modules.account;

import com.querydsl.core.types.Predicate;
import com.studyolle.modules.tag.Tag;
import com.studyolle.modules.zone.Zone;

import java.util.Set;

public class AccountPredicates {

    public static Predicate findByZonesAndTags(Set<Zone> zones, Set<Tag> tags) {
        QAccount account = QAccount.account;
        return account.zones.any().in(zones).and(account.tags.any().in(tags));
    }
}

package com.studyolle.modules.study;


import com.studyolle.modules.account.Account;
import com.studyolle.modules.account.UserAccount;
import com.studyolle.modules.event.Event;
import com.studyolle.modules.tag.Tag;
import com.studyolle.modules.zone.Zone;
import jakarta.persistence.*;
import lombok.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;




@Entity
@Getter
@Setter
@EqualsAndHashCode(of = "id") @AllArgsConstructor @NoArgsConstructor
public class Study {

    @Id
    @GeneratedValue
    private Long id;

    @OneToMany(mappedBy = "study", cascade = CascadeType.ALL)
    private List<Event> event = new ArrayList<>();

    @ManyToMany
    private Set<Account> managers = new HashSet<>();

    @ManyToMany
    private Set<Account> members = new HashSet<>();

    @Column(unique = true)
    private String path;

    private String title;

    private String shortDescription;

    @Lob
    @Basic(fetch = FetchType.EAGER)
    private String fullDescription;

    @Lob
    @Basic(fetch = FetchType.EAGER)
    private String image;

    @ManyToMany
    private Set<Tag> tags = new HashSet<>();

    @ManyToMany
    private Set<Zone> zones = new HashSet<>();

    private LocalDateTime publishedDateTime;

    private LocalDateTime closedDateTime;

    private LocalDateTime recruitingUpdatedDateTime;

    private boolean recruiting;

    private boolean published;

    private boolean closed;

    private boolean useBanner;

    @Column(nullable = false, columnDefinition = "integer default 0")
    private int memberCount;


    public void addManager(Account account) {
        this.managers.add(account);
    }

    public boolean isJoinable(UserAccount userAccount) {
        Account account = userAccount.getAccount();
        return this.isPublished() && this.isRecruiting()
                && !this.members.contains(account) && !this.managers.contains(account);
    }

    public boolean isMember(UserAccount userAccount) {
        return this.members.contains(userAccount.getAccount());
    }

    public boolean isManager(UserAccount userAccount) {
        return this.managers.contains(userAccount.getAccount());
    }


    public void publish() {
        if (!this.closed && !this.published) {
            this.published = true;
            this.publishedDateTime = LocalDateTime.now();
        } else {
            throw new IllegalStateException("스터디를 공개할 수 없는 상태입니다. 스터디를 이미 공개했거나 종료했습니다.");
        }
    }

    public void close() {
        if (this.published && !this.closed) {
            this.closed = true;
            this.published=false;
            this.closedDateTime = LocalDateTime.now();
        } else {
            throw new IllegalStateException("스터디를 종료할 수 없습니다. 스터디를 공개하지 않았거나 이미 종료한 스터디입니다.");
        }
    }

    public void startRecruit() {
        if (canUpdateRecruiting()) {
            this.recruiting = true;
            this.recruitingUpdatedDateTime = LocalDateTime.now();
        } else {
            throw new RuntimeException("인원 모집을 시작할 수 없습니다. 스터디를 공개하거나 한 시간 뒤 다시 시도하세요.");
        }

    }

    public void stopRecruit() {
        if (canUpdateRecruiting()) {
            this.recruiting = false;
            this.recruitingUpdatedDateTime = LocalDateTime.now();
        } else {
            throw new RuntimeException("인원 모집을 멈출 수 없습니다. 스터디를 공개하거나 한 시간 뒤 다시 시도하세요.");
        }
    }

    public boolean canUpdateRecruiting() {
        return this.published && this.recruitingUpdatedDateTime == null ||
                this.recruitingUpdatedDateTime.isBefore(LocalDateTime.now().minusHours(1));
    }

    public String getEncodedPath(){
        return URLEncoder.encode(this.path, StandardCharsets.UTF_8);
    }

    public boolean isRemovable() {
        return !this.published;  // TODO 모임을 했던 스터디는 삭제 불가.
    }

    public void addMember(Account account) {
        this.members.add(account);
        this.memberCount++;
    }

    public void removeMember(Account account) {
        this.members.remove(account);
        this.memberCount--;
    }

    public boolean isManagerBy(Account account) {
        return this.getManagers().contains(account);
    }
}

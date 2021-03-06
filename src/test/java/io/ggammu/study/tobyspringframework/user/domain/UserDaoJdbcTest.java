package io.ggammu.study.tobyspringframework.user.domain;

import io.younghwang.springframewokbasic.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TestDaoFactory.class})
class UserDaoJdbcTest {
    @Autowired
    private ApplicationContext context;
    @Autowired
    private UserDaoJdbc userDao;
    @Autowired
    private DataSource dataSource;

    private User user1;
    private User user2;
    private User user3;

    @BeforeEach
    public void setUp() {
        user1 = new User("0", "user1", "password1", Level.BASIC, 1, 0);
        user2 = new User("1", "user2", "password2", Level.SILVER, 55, 10);
        user3 = new User("2", "user3", "password3", Level.GOLD, 100, 40);
    }

    @Test
    public void 유저_추가_조회_확인() throws SQLException, ClassNotFoundException {
        userDao.deleteAll();
        assertThat(userDao.getCount()).isEqualTo(0);

        // then
        userDao.add(user1);
        userDao.add(user2);
        assertThat(userDao.getCount()).isEqualTo(2);

        User userGet1 = userDao.get(user1.getId());
        checkSameUser(userGet1, user1);

        User userGet2 = userDao.get(user2.getId());
        checkSameUser(userGet2, user2);
    }

    @Test
    public void 등록_유저수_확인() throws SQLException, ClassNotFoundException {
        // then
        userDao.deleteAll();
        assertThat(userDao.getCount()).isEqualTo(0);

        userDao.add(user1);
        assertThat(userDao.getCount()).isEqualTo(1);

        userDao.add(user2);
        assertThat(userDao.getCount()).isEqualTo(2);

        userDao.add(user3);
        assertThat(userDao.getCount()).isEqualTo(3);
    }

    @Test
    public void getUserFailure() throws SQLException, ClassNotFoundException {
        // given
        userDao.deleteAll();

        assertThat(userDao.getCount()).isEqualTo(0);

        // when
        Throwable thrown = catchThrowable(() -> userDao.get("unknown"));

        // then
        assertThat(thrown).isInstanceOf(EmptyResultDataAccessException.class);
    }

    @Test
    public void 모든_유저_조회() throws SQLException, ClassNotFoundException {
        userDao.deleteAll();

        List<User> users0 = userDao.getAll();
        assertThat(users0.size()).isEqualTo(0);

        userDao.add(user1);
        List<User> users1 = userDao.getAll();
        assertThat(users1.size()).isEqualTo(1);
        checkSameUser(user1, users1.get(0));

        userDao.add(user2);
        List<User> users2 = userDao.getAll();
        assertThat(users2.size()).isEqualTo(2);
        checkSameUser(user1, users1.get(0));
        checkSameUser(user2, users2.get(1));

        userDao.add(user3);
        List<User> users3 = userDao.getAll();
        assertThat(users3.size()).isEqualTo(3);
        checkSameUser(user1, users1.get(0));
        checkSameUser(user2, users2.get(1));
        checkSameUser(user3, users3.get(2));
    }

    private void checkSameUser(User user1, User user2) {
        assertThat(user1.getId()).isEqualTo(user2.getId());
        assertThat(user1.getName()).isEqualTo(user2.getName());
        assertThat(user1.getPassword()).isEqualTo(user2.getPassword());
        assertThat(user1.getLevel()).isEqualTo(user2.getLevel());
        assertThat(user1.getLogin()).isEqualTo(user2.getLogin());
        assertThat(user1.getRecommend()).isEqualTo(user2.getRecommend());
    }

    @Test
    public void 키_중복_확인() {
        // given
        userDao.deleteAll();
        userDao.add(user1);
//        userDao.add(user1);
        // when
        Throwable thrown = catchThrowable(() -> userDao.add(user1));

        // then
        assertThat(thrown).isInstanceOf(DataAccessException.class);
    }

    @Test
    public void 중복_예외_적용() {
        userDao.deleteAll();

        try {
            userDao.add(user1);
            userDao.add(user1);
        } catch (DuplicateKeyException ex) {
            SQLException sqlEx = (SQLException) ex.getRootCause();
            SQLExceptionTranslator set = new SQLErrorCodeSQLExceptionTranslator(this.dataSource);
            assertThat(set.translate(null, null, sqlEx)).isInstanceOf(DuplicateKeyException.class);
        }
    }

    @Test
    public void 사용자_수정() {
        userDao.deleteAll();

        userDao.add(user1);
        userDao.add(user2);

        user1.setName("modify");
        user1.setPassword("passwordm");
        user1.setLevel(Level.GOLD);
        user1.setLogin(1000);
        user1.setRecommend(9999);

        userDao.update(user1);

        User user1Update = userDao.get(user1.getId());
        checkSameUser(user1, user1Update);

        User user2Same = userDao.get(user2.getId());
        checkSameUser(user2, user2Same);
    }
}
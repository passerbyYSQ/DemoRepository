package top.ysqorz.r2dbc.dao;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;
import top.ysqorz.r2dbc.model.User;

public interface UserRepository extends ReactiveCrudRepository<User, String> {
    @Query("SELECT * FROM user WHERE email = :email")
    Mono<User> findByEmail(String email);
}
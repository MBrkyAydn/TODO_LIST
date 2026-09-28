package com.berkay.todo.repository;

import com.berkay.todo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
/*Spring Data JPA burada method isminden ne istediğimizi anlayabiliyor.
Kullanıcı bulundu
       ↓
Optional<User> → User

Kullanıcı bulunamadı
       ↓
Optional.empty()
*/

}

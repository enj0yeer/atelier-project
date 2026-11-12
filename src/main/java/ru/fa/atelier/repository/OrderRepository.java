package ru.fa.atelier.repository;

import ru.fa.atelier.entity.Order;
import ru.fa.atelier.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    @EntityGraph(attributePaths = {"client", "employee", "items", "items.service"})
    List<Order> findAllByOrderByOrderDateDesc();

    @EntityGraph(attributePaths = {"client", "items", "items.service"})
    List<Order> findAllByClient_User_EmailOrderByOrderDateDesc(String email);

    @EntityGraph(attributePaths = {"client", "employee", "items", "items.service"})
    @Query("select distinct o from AtelierOrder o join o.client c left join o.items i left join i.service s " +
           "where (:status is null or o.status = :status) " +
           "and (:search = '' or lower(c.fullName) like lower(concat('%', :search, '%')) " +
           "or lower(s.name) like lower(concat('%', :search, '%'))) order by o.orderDate desc")
    List<Order> searchAll(@Param("status") OrderStatus status, @Param("search") String search);

    @EntityGraph(attributePaths = {"client", "items", "items.service"})
    @Query("select distinct o from AtelierOrder o join o.client c left join o.items i left join i.service s " +
           "where c.user.email = :email and (:status is null or o.status = :status) " +
           "and (:search = '' or lower(s.name) like lower(concat('%', :search, '%'))) " +
           "order by o.orderDate desc")
    List<Order> searchForClient(@Param("email") String email,
                                @Param("status") OrderStatus status,
                                @Param("search") String search);

    long countByStatus(OrderStatus status);

    @Query("select coalesce(sum(o.totalPrice), 0) from AtelierOrder o")
    BigDecimal calculateTotalRevenue();
}

package org.example.repository;

import org.example.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    // Ищем CartItem по cart.id и book.id
    CartItem findByCart_IdAndBook_Id(Long cartId, Long bookId);
}

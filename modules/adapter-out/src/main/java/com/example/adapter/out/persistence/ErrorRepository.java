package com.example.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA repository interface for managing ErrorEntity persistence.
 * <p>
 * Provides CRUD operations and query methods for ErrorEntity objects, enabling
 * persistent storage and retrieval of processing errors.
 * <p>
 * <b>Responsibilities:</b>
 * <ul>
 *   <li>Supports saving, updating, deleting, and finding ErrorEntity records</li>
 *   <li>Enables integration with Spring Data JPA for error management</li>
 * </ul>
 * <b>Usage:</b> Used by persistence adapters and services to manage error records in the database.
 */
@Repository
public interface ErrorRepository extends JpaRepository<ErrorEntity, String> {
} 
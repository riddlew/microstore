package dev.riddle.microstore.inventory.inventory.item;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "inventory_item", indexes = {
	@Index(name = "idx_sku", columnList = "sku")
})
@Getter
@Setter
public class InventoryItem {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(nullable = false, unique = true, length = 12)
	private String sku;

	@Column(nullable = false)
	private String name;

	@Column
	private String description;

	@Column(nullable = false)
	private int quantity;

	@Column(nullable = false)
	private int priceInCents;

	@CreationTimestamp
	@Column(name = "created_at")
	private Instant createdAt = Instant.now();

	@UpdateTimestamp
	@Column(name = "updated_at")
	private Instant updatedAt = Instant.now();
}

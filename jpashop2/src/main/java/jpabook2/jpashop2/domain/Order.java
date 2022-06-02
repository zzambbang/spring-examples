package jpabook2.jpashop2.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "orders")
@Getter
@Setter
public class Order {

	@Id
	@GeneratedValue
	@Column(name = "order_id")
	private Long id;

	//다대일 관계
	@ManyToOne(fetch = FetchType.LAZY) //EAGER는 조인을 한꺼번에 해서 가져온다.
	@JoinColumn(name = "member_id") //foreignkey 매핑해주기 연관관계 주인도 정해줘야함
	private Member member;

	@OneToMany(mappedBy = "order") //나는 거울이야..
	private List<OrderItem> orderItems = new ArrayList<>();

	@OneToOne(fetch = FetchType.LAZY) //onetoone 이라도 연관관계 주인 잡아줘야함. 가까운거 잡아주면 좋지 ~
	@JoinColumn(name = "delivery_id")
	private Delivery delivery;

	private LocalDateTime orderDate; //주문시간

	@Enumerated(EnumType.STRING)
	private OrderStatus status; //주문상태 [ORDER, CANCEL]
}

package jpabook.jpashop.domain;

import static javax.persistence.CascadeType.*;
import static javax.persistence.FetchType.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
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

import org.hibernate.annotations.BatchSize;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "orders")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

	@Id @GeneratedValue
	@Column(name = "order_id")
	private Long id;

	//order member 다대일
	@ManyToOne(fetch = LAZY)
	@JoinColumn(name = "member_id") //근데 양방향 관계니까 연관관계 주인을 정해줘야한다. 외래키 있는곳을 주인으로
	private Member member;

	//@BatchSize(size = 1000) //컬렉션은 이렇게 해주면 됨 아닌 경우(item)확인
	@OneToMany(mappedBy = "order", cascade = ALL) //persist를 한번에 해준다
	private List<OrderItem> orderItems = new ArrayList<>();

	@OneToOne(fetch = LAZY, cascade = ALL) //일대일관계에서는 외래키를 어디에나 둬도 된다. 주로 액세스많이 하는곳에 하는걸 추천합니다.
	@JoinColumn(name = "delivery_id")
	private Delivery delivery; //order를 저장할때 delivery도 persist해준다.

	//날짜관련 어노테이션 매핑해야하는데 자바 8 이후로는 ㄱㅊ음
	//private Date date;
	private LocalDateTime orderDate;

	@Enumerated(EnumType.STRING)
	private OrderStatus status; //주문상태 [ORDER, CANCLE]

	//==연관관계 편이 메서드==// 위치 : 컨트롤을 핵심적으로 하는곳에 둬야한다. 양방향에서는 있는게 좋다. 양방향 셋팅
	public void setMember(Member member) { //양방향을 넣어줘야해
		this.member = member;
		member.getOrders().add(this); // 양방향 연관관계에 다 걸리는것
	}

	public void addOrderItem(OrderItem orderItem) {
		orderItems.add(orderItem);
		orderItem.setOrder(this);
	}

	public void setDelivery(Delivery delivery) {
		this.delivery = delivery;
		delivery.setOrder(this);
	}

	//원래 코드라면 이렇게
	// public static void main(String[] args) {
	// 	Member member = new Member();
	// 	Order order = new Order();
	// 	member.getOrders().add(order);
	// 	order.setMember(member);
	// }

	//==생성 메서드==//
	public static Order createOrder(Member member, Delivery delivery, OrderItem... orderItems) {
		Order order = new Order();
		order.setMember(member);
		order.setDelivery(delivery);
		for (OrderItem orderItem : orderItems) {
			order.addOrderItem(orderItem);
		}
		order.setStatus(OrderStatus.ORDER); //처음엔 order상태로 강제할거예요
		order.setOrderDate(LocalDateTime.now());
		return order;
	}

	//==비즈니스 로직==//
	/**
	* 주문 취소
	*/
	public void cancel() {
		if (delivery.getStatus() == DeliveryStatus.COMP) {
			throw new IllegalStateException("이미 배송 완료된 상품은 취소가 불가능합니다");
		}

		this.setStatus(OrderStatus.CANCEL);
		for (OrderItem orderItem : orderItems) {
			orderItem.cancel(); //orderitem 각각에 cancel 날려주는 것
		}
	}

	//==조회 로직==//
	/**
	* 전체 주문 가격 조회
	*/
	public int getTotalPrice() {
		int totalPrice = 0;
		for (OrderItem orderItem : orderItems) {
			totalPrice += orderItem.getTotalPrice();
		}
		return totalPrice;
	}
}

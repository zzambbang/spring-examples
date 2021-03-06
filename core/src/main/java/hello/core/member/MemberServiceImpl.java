package hello.core.member;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MemberServiceImpl implements MemberService {

	private final MemberRepository memberRepository; //인터페이스만 잇죠? 추상화에만 의존하고잇음 생성자를 통해서 객체가 들어간다 DI

	@Autowired //ac.getBean(MemberRepository.class) 마치 이거처럼 들어간다.
	public MemberServiceImpl(MemberRepository memberRepository) {
		this.memberRepository = memberRepository;
	}

	@Override
	public void join(Member member) {
		memberRepository.save(member); //다형성으로 new 한게 호출이 되겟죠?
	}

	@Override
	public Member findMember(Long memberId) {
		return memberRepository.findById(memberId);
	}

	//테스트 용도
	public MemberRepository getMemberRepository() {
		return memberRepository;
	}
}

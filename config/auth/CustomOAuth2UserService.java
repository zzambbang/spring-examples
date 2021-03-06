package recorder.backend.config.auth;

import java.util.Collections;

import javax.servlet.http.HttpSession;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import recorder.backend.config.auth.dto.OAuthAttributes;
import recorder.backend.config.auth.dto.SessionUser;
import recorder.backend.domain.user.User;
import recorder.backend.repository.UserRepository;

@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
	private final UserRepository userRepository;
	private final HttpSession httpSession;

	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

		OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
		OAuth2User oAuth2User = delegate.loadUser(userRequest);

		//현재 로그인 진행중인 서비스를 구분하는 코드(지금은 구글만 이지만, 이후 네이버 등 연동할때 필요)
		String registrationId = userRequest.getClientRegistration().getRegistrationId();
		//userNameAttributeName -> OAuth2 로그인 진행 시 키가 되는 필드값. primary key 와 같다. 구글의 기본코드는 sub
		String userNameAttributeName = userRequest.getClientRegistration()
			.getProviderDetails()
			.getUserInfoEndpoint()
			.getUserNameAttributeName();

		//OAuth2UserService를 통해 가져온 OAuth2User의 attribute를 담을 클래스이다.
		OAuthAttributes attributes = OAuthAttributes.of(registrationId, userNameAttributeName, oAuth2User.getAttributes());

		User user = saveOrUpdate(attributes);

		//sessionUser 세션에 사용자 정보를 저장하기 위한 dto 클래스.
		httpSession.setAttribute("user", new SessionUser(user));
		return new DefaultOAuth2User(
			Collections.singleton(new SimpleGrantedAuthority(user.getRoleKey())),
			attributes.getAttributes(),
			attributes.getNameAttributeKey()
		);
	}

	private User saveOrUpdate(OAuthAttributes attributes) {
		User user = userRepository.findByEmail(attributes.getEmail())
			.map(entity -> entity.update(attributes.getName(), attributes.getPicture()))
			.orElse(attributes.toEntity());

		return userRepository.save(user);
	}
}

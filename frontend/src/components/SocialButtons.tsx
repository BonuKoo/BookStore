import { socialLoginUrl } from '../api/auth';
import { SOCIAL_PROVIDERS } from '../util/constants';

/** 소셜 로그인 버튼 묶음 — 각 제공자별 시작 URL로 이동하는 링크 */
export default function SocialButtons() {
  return (
    <div className="social-buttons">
      {SOCIAL_PROVIDERS.map((provider) => (
        <a
          key={provider.id}
          className={`btn btn-social ${provider.id}`}
          href={socialLoginUrl(provider.id)}
        >
          {provider.label}
        </a>
      ))}
    </div>
  );
}

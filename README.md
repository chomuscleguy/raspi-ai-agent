# 🤖 Raspi AI Agent

라즈베리파이(Ubuntu Server 64-bit / ARM64) 위에서 동작하는 **AI 에이전트 백엔드**입니다.
Spring Boot 3 + Spring AI + Google Gemini를 기반으로, 개인 하드웨어에 프로덕션 수준의
컨테이너 인프라와 CI/CD 파이프라인을 직접 구축한 포트폴리오 프로젝트입니다.

> "클라우드 없이, 집에 있는 라즈베리파이 한 대로 실제 서비스처럼 배포까지 해보자"는 목표로 시작했습니다.

---

## 📌 프로젝트 개요

| 항목 | 내용 |
|---|---|
| 목표 | 라즈베리파이에 풀스택 AI 에이전트 백엔드를 컨테이너 기반으로 배포 |
| 하드웨어 | Raspberry Pi (Ubuntu Server 64-bit, ARM64) |
| 백엔드 | Java 17, Spring Boot 3.3.4, Spring AI 1.1.2 |
| AI 모델 | Google Gemini (`gemini-3.6-flash`, Google AI Studio API) |
| 인프라 | Docker Compose (PostgreSQL 16, Redis 7, Nginx, Spring Boot App) |
| CI/CD | GitHub Actions → Docker Hub → Tailscale VPN → SSH 자동 배포 |
| 개발 환경 | Windows + IntelliJ IDEA, SSH로 라즈베리파이 원격 제어 |

---

## 🏗️ 아키텍처

```
[개발 PC / IntelliJ]
        │  git push (main)
        ▼
[GitHub Actions]
   ① Gradle 빌드 + 테스트 (JDK 17)
   ② Docker Buildx + QEMU로 linux/arm64 이미지 빌드 → Docker Hub push
   ③ Tailscale VPN 연결 → SSH로 라즈베리파이 접속 → 배포 스크립트 실행
        │
        ▼
[라즈베리파이 (Docker Compose)]
   ┌──────────────────────────────────────────────┐
   │  Nginx (Reverse Proxy, 80/443)                │
   │        │                                      │
   │        ▼                                      │
   │  Spring Boot App (8080)                       │
   │   ├─ Spring AI ChatClient → Google Gemini API │
   │   ├─ Custom Tool: JVM/시스템 상태 조회         │
   │   ├─ PostgreSQL 16 (대화 이력 저장)            │
   │   └─ Redis 7 (세션/캐시)                       │
   └──────────────────────────────────────────────┘
```

---

## ✨ 주요 기능

- **`POST /api/v1/chat`** — 사용자 메시지를 받아 Gemini 모델의 응답을 반환하는 대화형 API
- **Function Calling (Tool Use)** — AI가 대화 중 필요하다고 판단하면 자동으로 `SystemStatusTool`을
  호출해, 라즈베리파이의 실제 JVM 힙 메모리·CPU 부하·코어 수를 조회하고 답변에 반영
- **Actuator 헬스체크** — `/actuator/health`로 DB, Redis, 앱 상태를 외부에서 모니터링 가능
- **완전 자동화된 CI/CD** — `main` 브랜치에 push하면 빌드→이미지화→배포까지 사람 개입 없이 진행

### 실제 동작 예시

```bash
curl -X POST http://localhost/api/v1/chat \
  -H "Content-Type: application/json" \
  -d '{"message":"지금 서버 메모리 상태 좀 알려줘"}'
```

```json
{
  "conversationId": "4bca94c9-...",
  "reply": "현재 서버(JVM)의 메모리 상태는 다음과 같습니다: 최대 힙 메모리 1,016MB, 현재 사용 중인 힙 메모리 81MB, ... 현재 메모리 및 CPU 부하 상태는 안정적입니다!"
}
```

---

## 🛠️ 기술 스택

**Backend**
- Java 17, Spring Boot 3.3.4
- Spring AI 1.1.2 (`spring-ai-starter-model-google-genai`)
- Spring Data JPA, Spring Data Redis
- Spring Boot Actuator

**Infra**
- Docker / Docker Compose (멀티 컨테이너, ARM64 네이티브 이미지)
- PostgreSQL 16, Redis 7 (Alpine), Nginx 1.27 (Alpine)
- Gradle (멀티스테이지 Dockerfile 빌드)

**CI/CD & 배포**
- GitHub Actions (3-stage 파이프라인: 빌드/테스트 → 이미지 빌드/푸시 → SSH 배포)
- Docker Buildx + QEMU (크로스 아키텍처 ARM64 빌드)
- Docker Hub (이미지 레지스트리)
- Tailscale (VPN 기반 안전한 원격 SSH 접근)

---

## 🚀 로컬/배포 환경 구성

### 1. 인프라 기동 (라즈베리파이)

```bash
git clone https://github.com/<your-id>/raspi-ai-agent.git
cd raspi-ai-agent/docker
cp .env.example .env
nano .env   # 실제 DB 비밀번호, GOOGLE_AI_API_KEY 등 입력
docker compose up -d
```

### 2. 환경 변수 (`docker/.env`)

```bash
TZ=Asia/Seoul
DOCKER_USERNAME=your-dockerhub-id
POSTGRES_USER=agent_admin
POSTGRES_PASSWORD=********
POSTGRES_DB=agent_db
REDIS_PASSWORD=********
GOOGLE_AI_API_KEY=********
```

> ⚠️ `.env`는 절대 git에 커밋하지 않습니다 (`.gitignore` 처리). 값이 비어있는 `.env.example`만 저장소에 포함됩니다.

### 3. CI/CD 필요 GitHub Secrets

| Secret | 설명 |
|---|---|
| `DOCKER_USERNAME` / `DOCKER_PASSWORD` | Docker Hub 로그인 (Access Token 권장) |
| `SERVER_HOST` | 라즈베리파이 Tailscale IP (`100.x.x.x`) |
| `SERVER_USERNAME` / `SERVER_SSH_KEY` / `SERVER_SSH_PORT` | SSH 배포 계정 정보 |
| `SERVER_DEPLOY_PATH` | 라즈베리파이 내 프로젝트 경로 |
| `TAILSCALE_OAUTH_CLIENT_ID` / `TAILSCALE_OAUTH_SECRET` | GitHub Actions 러너가 라즈베리파이 내부망에 접속하기 위한 Tailscale OAuth 인증 |

---

## 🧯 트러블슈팅 기록

실제로 이 프로젝트를 구축하며 겪었던 문제와 해결 과정입니다. 하나하나가 "혼자 배우면서
실무형 인프라를 만들 때 생기는 진짜 문제들"이라 기록으로 남깁니다.

### 1. GitHub Push Protection에 API 키가 걸림

`application.yml`에 테스트용으로 Google API 키를 잠깐 하드코딩했다가, GitHub의 시크릿 스캐닝이
push 자체를 차단했습니다. `git commit --amend`로는 해결이 안 됐는데, 알고 보니 문제의 커밋이
가장 최근 커밋이 아니라 몇 개 전 커밋이었기 때문이었습니다. 결국 `.git` 폴더를 통째로 삭제하고
이력을 새로 시작해 문제의 커밋 자체를 이력에서 제거했고, 노출됐던 키는 즉시 폐기 후 재발급했습니다.

> **배운 점**: 시크릿은 애초에 코드에 하드코딩하지 않는 게 최선이지만, 실수로 커밋했다면
> `amend`가 최근 커밋에만 적용된다는 점을 인지하고, 필요하면 이력 자체를 재작성해야 합니다.

### 2. `docker/login-action`이 계속 "Username and password required" 에러

GitHub Secrets에 `DOCKER_USERNAME`, `DOCKER_PASSWORD`를 분명히 등록했는데도 실패했습니다.
원인은 Docker Hub 토큰을 복사하는 과정에서 값이 온전히 저장되지 않았던 것으로 추정되며,
Secret을 삭제 후 재등록하고 토큰도 재발급하여 해결했습니다.

> **배운 점**: GitHub Secrets는 저장 후 값을 다시 확인할 방법이 없어서, 의심되면 지우고
> 새로 등록하는 게 가장 빠른 디버깅 방법입니다.

### 3. SSH 배포 단계에서 `dial tcp: i/o timeout`

로컬 네트워크(같은 공유기)에서는 SSH 접속이 잘 됐지만, GitHub Actions 러너(외부 인터넷)에서는
`192.168.x.x` 내부 IP로 절대 접속할 수 없었습니다. 공유기 포트포워딩 대신 **Tailscale VPN**을
도입해, 라즈베리파이와 GitHub Actions 러너를 같은 가상 사설망으로 묶는 방식으로 해결했습니다.
이후 Tailscale 인증 방식을 Auth Key에서 **OAuth Client** 방식으로 전환해 보안성을 높였습니다.

> **배운 점**: 사설 네트워크 안의 서버를 외부 CI에서 접근하게 하려면 포트포워딩보다
> VPN 오버레이 네트워크가 훨씬 안전하고 유지보수하기 쉽습니다.

### 4. Force Push 이후 라즈베리파이의 `git pull`이 "divergent branches" 에러

로컬 이력을 재작성(`--force` push)한 뒤, 라즈베리파이에 남아있던 옛 이력과 원격 이력이
서로 다른 뿌리를 갖게 되어 `git pull`이 자동 병합을 거부했습니다. 배포 스크립트를
`git pull` 대신 `git fetch && git reset --hard origin/main`으로 바꿔, 라즈베리파이 쪽은
항상 원격 상태를 그대로 반영하도록 수정해 재발을 방지했습니다.

### 5. `docker compose up -d --no-deps app`이 의존 서비스를 다 건너뜀

배포 스크립트가 `app` 컨테이너만 재기동하도록 `--no-deps` 옵션을 썼는데, 애초에
PostgreSQL·Redis·Nginx가 한 번도 기동된 적 없는 상태에서는 `app`만 덩그러니 뜨고
나머지는 계속 비어있는 상황이 발생했습니다. `--no-deps`를 빼고 전체 스택을 `up -d`로
띄우도록 배포 스크립트를 수정했습니다.

### 6. Nginx가 요청을 받고도 계속 연결이 끊김

curl 요청이 계속 `Recv failure`로 끊겼는데, 알고 보니 준비되지 않은 SSL 인증서를
요구하는 443 서버 블록으로 80번 포트 요청을 강제 리다이렉트하고 있었습니다.
아직 도메인/인증서가 없는 개발 단계였기에, 리다이렉트 없이 80번 포트에서 바로
`app:8080`으로 프록시하도록 nginx 설정을 단순화했습니다.

### 7. Gemini 모델 Deprecation 릴레이

`gemini-2.0-flash-001`을 쓰고 있었는데, Google이 이 모델을 완전히 서비스 종료하면서
404 에러가 발생했습니다. `gemini-3.6-flash`로 모델명을 바꿨지만 여전히 같은 에러가
반복됐는데, 원인은 모델명을 지정하는 **프로퍼티 경로 자체가 틀렸기 때문**이었습니다.

```yaml
# ❌ 무시됨 (Spring AI 1.1.2 기준)
spring.ai.google.genai.chat.model: gemini-3.6-flash

# ✅ 올바른 경로
spring.ai.google.genai.chat.options.model: gemini-3.6-flash
```

> **배운 점**: 빠르게 발전하는 라이브러리(Spring AI)는 마이너 버전 사이에도 설정 키 경로가
> 바뀔 수 있어, 공식 문서나 실제 이슈 트래커를 항상 최신 버전 기준으로 재확인해야 합니다.

### 8. GitHub Actions는 성공했는데 라즈베리파이 이미지가 안 바뀜

워크플로우가 초록불로 성공 표시됐는데도 라즈베리파이의 이미지 ID가 그대로였습니다.
Docker Buildx의 GitHub Actions 캐시(`cache-from`/`cache-to: type=gha`)가 코드 변경을
제대로 감지하지 못해 예전 레이어를 재사용한 것으로 추정, `no-cache: true`로 강제
전체 재빌드하여 해결했습니다.

---

## 📂 프로젝트 구조

```
raspi-ai-agent/
├── .github/workflows/deploy.yml     # CI/CD 파이프라인
├── Dockerfile                        # 멀티스테이지 빌드 (JDK → JRE)
├── build.gradle
├── docker/
│   ├── docker-compose.yml           # postgres, redis, app, nginx
│   ├── .env.example
│   ├── db/init/01-init.sql          # 최초 DB 스키마
│   └── nginx/conf.d/default.conf
└── src/
    ├── main/java/com/chomu/raspiaiagent/
    │   ├── controller/ChatController.java
    │   ├── service/ChatAgentService.java
    │   └── tool/SystemStatusTool.java
    └── main/resources/application.yml
```

---

## 🔭 향후 개선 계획

- [ ] Let's Encrypt 기반 HTTPS 적용 (현재는 개발 단계라 HTTP만 지원)
- [ ] `InMemoryChatMemory` → Redis 기반 영속 대화 메모리로 전환
- [ ] PostgreSQL 기반 대화 이력 저장/조회 API 추가
- [ ] Actuator + Prometheus/Grafana 연동 모니터링 대시보드

---

## 📝 라이선스

개인 학습 및 포트폴리오 목적의 프로젝트입니다.

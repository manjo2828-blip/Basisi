# AI 시터 추천 (OpenAI) — 팀원 로컬 실행 가이드

OpenAI GPT API를 연동한 **AI 시터 추천** 기능을 로컬에서 쓰는 방법입니다.

> **중요:** API 키는 GitHub에 올라가지 않습니다.  
> 각자 PC에 `scripts/local-secrets.ps1` 파일을 만들어 키를 넣어야 GPT 추천이 동작합니다.

---

## 1. 필요한 것

| 항목 | 버전/설명 |
|------|-----------|
| Java | 17 이상 |
| Node.js | 18 이상 권장 |
| OpenAI API 키 | [OpenAI Platform](https://platform.openai.com/api-keys)에서 발급 |

API 키가 없어도 **앱 자체는 실행**됩니다. 다만 추천은 GPT 없이 **기본 알고리즘**만 사용됩니다.

---

## 2. 코드 받기

```powershell
git pull origin main
```

(브랜치 이름이 다르면 해당 브랜치로 pull)

---

## 3. OpenAI API 키 설정 (GPT 쓰려면 필수)

### Windows (PowerShell)

프로젝트 루트에서:

```powershell
Copy-Item scripts/local-secrets.example.ps1 scripts/local-secrets.ps1
notepad scripts/local-secrets.ps1
```

`local-secrets.ps1` 내용 예시:

```powershell
$env:BASISI_OPENAI_API_KEY = "sk-여기에_본인_또는_팀_키"
```

저장 후 닫기.

> `local-secrets.ps1`은 `.gitignore`에 등록되어 **절대 Git에 커밋되지 않습니다.**

### Mac / Linux

터미널에서 환경변수로 설정:

```bash
export BASISI_OPENAI_API_KEY="sk-여기에_키"
```

(매 터미널마다 설정해야 하므로, `~/.zshrc` 등에 넣어두면 편합니다.)

---

## 4. 백엔드 실행

### Windows — 권장

```powershell
cd 프로젝트_루트_경로
.\scripts\start-backend.ps1
```

콘솔에 아래가 보이면 성공:

```
[start-backend] loaded local-secrets.ps1
[start-backend] BASISI_OPENAI_API_KEY is set.
Tomcat started on port 8080
```

키가 없으면:

```
BASISI_OPENAI_API_KEY is empty. OpenAI LLM will use algorithm fallback only.
```

→ 앱은 돌아가지만 GPT 추천은 **안 됩니다.**

### Mac / Linux

```bash
cd 프로젝트_루트_경로
export BASISI_OPENAI_API_KEY="sk-..."
./gradlew bootRun
```

---

## 5. 프론트 실행 (새 터미널)

```powershell
cd frontend
npm install
npm run dev
```

터미널에 나온 주소로 접속 (예: `http://localhost:5173`).

---

## 6. 사이트에서 확인

1. **PARENT(부모)** 계정으로 로그인  
   - SITTER 계정은 AI 추천 영역이 비어 있거나 제한됩니다.
2. 왼쪽 메뉴 **「시터 탐색」** (`/search`) 이동
3. **「✨ AI 추천 시터」** 영역 확인
4. GPT가 쓴 **자연스러운 한국어 추천 문장**이 보이면 성공

### 개발자 도구로 API 확인 (선택)

1. **F12** → **Network** 탭
2. 필터: `sitters`
3. `sitters` 요청 → **Response**

**GPT 성공:**

```json
"source": "LLM",
"llmFallback": false
```

**GPT 실패 (기본 추천으로 대체):**

```json
"source": "ALGORITHM",
"llmFallback": true
```

화면에 파란 배너 **「AI 분석 연결에 문제가 있어…」** 가 뜹니다.

**API 키 없음 (GPT 미사용):**

```json
"source": "ALGORITHM",
"llmFallback": false
```

---

## 7. 자주 하는 실수

| 증상 | 원인 | 해결 |
|------|------|------|
| GPT 문장 안 나옴 | API 키 미설정 | `local-secrets.ps1` 생성 후 `start-backend.ps1` 사용 |
| 파란 fallback 배너 | GPT 호출 실패 / rate limit | 1분 후 F5, 백엔드 재시작 |
| `gradlew bootRun`만 실행 | 환경변수 안 읽힘 | Windows는 `start-backend.ps1` 사용 |
| AI 영역 비어 있음 | SITTER 계정 | PARENT로 로그인 |
| 예전 화면 그대로 | 백엔드 미재시작 | 코드 pull 후 백엔드 재시작 |

---

## 8. API 키 팀 공유 방법

- GitHub / 코드 저장소에 **키를 넣지 마세요.**
- 팀 카톡 DM, 노션(비공개), 1Password 등 **별도 채널**로 공유
- 팀 공용 키 1개를 쓰거나, 각자 OpenAI 계정에서 발급

---

## 9. 한 줄 요약

```
git pull → local-secrets.ps1에 키 → start-backend.ps1 → npm run dev → PARENT 로그인 → /search
```

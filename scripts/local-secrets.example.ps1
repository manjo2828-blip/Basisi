# 이 파일을 복사해 local-secrets.ps1 로 저장한 뒤 OpenAI API 키를 넣으세요.
# local-secrets.ps1 은 .gitignore 대상이라 Git에 올라가지 않습니다.
#
#   Copy-Item scripts/local-secrets.example.ps1 scripts/local-secrets.ps1
#   notepad scripts/local-secrets.ps1

$env:BASISI_OPENAI_API_KEY = "sk-여기에_OpenAI_API_키"

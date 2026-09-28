from pydantic_settings import BaseSettings

class Settings(BaseSettings):
    PROJECT_NAME: str = "Campus360 AI Engine"
    DATABASE_URL: str = "postgresql://postgres:postgrespassword@localhost:5432/campus360"
    LIVENESS_THRESHOLD: float = 100.0  # Blur/spoof variance threshold

settings = Settings()
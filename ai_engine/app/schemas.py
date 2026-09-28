from pydantic import BaseModel
from uuid import UUID
from datetime import datetime

class AttendanceVerificationResponse(BaseModel):
    status: str
    student_id: UUID
    course_code: str
    confidence_score: float
    liveness_verified: bool
    liveness_score: float
    timestamp: datetime
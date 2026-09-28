import cv2
import numpy as np
from app.config import settings

class LivenessDetector:
    @staticmethod
    def calculate_blur_variance(image_bytes: bytes) -> float:
        nparr = np.frombuffer(image_bytes, np.uint8)
        img = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
        
        if img is None:
            raise ValueError("Invalid image file or unsupported format.")
            
        gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
        variance = cv2.Laplacian(gray, cv2.CV_64F).var()
        return float(variance)

    @classmethod
    def verify_liveness(cls, image_bytes: bytes) -> tuple[bool, float]:
        score = cls.calculate_blur_variance(image_bytes)
        is_live = score >= settings.LIVENESS_THRESHOLD
        return is_live, round(score, 2)
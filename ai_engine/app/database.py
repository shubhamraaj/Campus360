import asyncpg
from app.config import settings

class DatabasePool:
    def __init__(self):
        self.pool = None

    async def connect(self):
        self.pool = await asyncpg.create_pool(
            dsn=settings.DATABASE_URL,
            min_size=2,
            max_size=10
        )

    async def disconnect(self):
        if self.pool:
            await self.pool.close()

db = DatabasePool()
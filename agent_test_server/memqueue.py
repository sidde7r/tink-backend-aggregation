from Queue import Queue, Empty

class MemoryMessageQueue:
    def __init__(self):
        self.queues = {}

    def put(self, topic, message):
        queue = self.queues.setdefault(topic, Queue())
        queue.put_nowait(message)

    def get(self, topic, timeout_seconds):
        queue = self.queues.setdefault(topic, Queue())
        try:
            return queue.get(block=True, timeout=timeout_seconds)
        except Empty, e:
            return None




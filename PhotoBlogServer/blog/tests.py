from django.test import TestCase

# Create your tests here.
from django.contrib.auth import get_user_model
from django.core.files.uploadedfile import SimpleUploadedFile
from rest_framework import status
from rest_framework.authtoken.models import Token
from rest_framework.test import APITestCase

User = get_user_model()

class PhotoBlogAPITest(APITestCase):
    def setUp(self):
        # 1. 테스트용 가상 유저 및 토큰 자동 생성
        self.user = User.objects.create_user(username='testuser', password='password123')
        self.token = Token.objects.create(user=self.user)
        
        # 2. DRF 전용 테스트 클라이언트에 토큰 헤더 설정
        self.client.credentials(HTTP_AUTHORIZATION=f'Token {self.token.key}')
        self.url = '/api_root/Post/'

    def test_01_get_posts(self):
        """게시글 목록 조회 (GET) 검증"""
        response = self.client.get(self.url)
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertIsInstance(response.data, list)

    def test_02_create_post(self):
        """게시글 및 이미지 업로드 (POST) 검증"""
        # ImageField 검증을 통과하는 최소 크기의 실제 1x1 GIF 바이트
        valid_gif = (
            b"\x47\x49\x46\x38\x39\x61\x01\x00\x01\x00\x00\x00\x00\x21\xf9\x04"
            b"\x01\x0a\x00\x01\x00\x2c\x00\x00\x00\x00\x01\x00\x01\x00\x00\x02"
            b"\x02\x4c\x01\x00\x3b"
        )
        image = SimpleUploadedFile("test.gif", valid_gif, content_type="image/gif")
        
        # 주의: 모델의 author가 ForeignKey라면 self.user.id 사용, CharField라면 'testuser' 사용
        data = {'author': self.user.id, 'title': '테스트 제목', 'text': 'DRF APITestCase 테스트', 'image': image }
        
        response = self.client.post(self.url, data, format='multipart')
        self.assertEqual(response.status_code, status.HTTP_201_CREATED)
        self.assertEqual(response.data.get('text'), 'DRF APITestCase 테스트')
/** @type {import('next').NextConfig} */
const nextConfig = {
  reactStrictMode: true,
  // 빌드 캐시 무효화를 위한 설정
  webpack: (config) => {
    // 경로 별칭이 제대로 해석되도록 설정
    return config;
  },
};

export default nextConfig;


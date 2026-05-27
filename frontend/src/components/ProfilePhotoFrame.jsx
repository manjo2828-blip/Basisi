import React from 'react';

const SIZE_MAP = {
  sm: 72,
  md: 96,
  lg: 120,
  hero: 136
};

/**
 * 시터 프로필 사진 공통 프레임 (3:4 세로 비율).
 */
export function ProfilePhotoFrame({
  src,
  alt = '프로필 사진',
  size = 'md',
  border = 'solid',
  dimmed = false,
  children
}) {
  const width = SIZE_MAP[size] || SIZE_MAP.md;
  const borderStyle =
    border === 'dashed'
      ? '1px dashed rgba(199, 61, 106, 0.45)'
      : border === 'accent'
        ? '2px solid rgba(199, 61, 106, 0.28)'
        : '1px solid rgba(0, 0, 0, 0.08)';

  return (
    <div
      style={{
        width,
        aspectRatio: '3 / 4',
        borderRadius: 12,
        overflow: 'hidden',
        flexShrink: 0,
        border: borderStyle,
        background: 'rgba(26, 21, 35, 0.05)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        opacity: dimmed ? 0.85 : 1
      }}
    >
      {src ? (
        <img
          src={src}
          alt={alt}
          style={{
            width: '100%',
            height: '100%',
            objectFit: 'cover',
            objectPosition: 'center top'
          }}
        />
      ) : (
        children
      )}
    </div>
  );
}

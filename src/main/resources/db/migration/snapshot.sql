-- Create enrolled_snapshots table
CREATE TABLE IF NOT EXISTS enrolled_snapshots (
    id BIGSERIAL PRIMARY KEY,
    enrollment_id BIGINT NOT NULL UNIQUE,
    original_course_id VARCHAR(255),
    course_title VARCHAR(255) NOT NULL,
    course_description TEXT,
    course_image_url TEXT,
    course_video_url TEXT,
    instructor_name VARCHAR(255),
    instructor_id VARCHAR(255),
    structure_json TEXT NOT NULL,
    course_version_at TIMESTAMP,
    version INTEGER DEFAULT 1,
    upgraded_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW(),
    CONSTRAINT fk_enrolled_snapshot_enrollment 
        FOREIGN KEY (enrollment_id) 
        REFERENCES enrollments(id) 
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_enrolled_snapshots_enrollment ON enrolled_snapshots(enrollment_id);
CREATE INDEX IF NOT EXISTS idx_enrolled_snapshots_original_course ON enrolled_snapshots(original_course_id);

-- User Answers: SET NULL (preserve answers when test deleted)
ALTER TABLE user_answers DROP CONSTRAINT IF EXISTS fkq9ubv2ar56hkwxokdbp72b5by;
ALTER TABLE user_answers DROP CONSTRAINT IF EXISTS fk_user_answers_answer;
ALTER TABLE user_answers DROP CONSTRAINT IF EXISTS user_answers_answer_id_fkey;
ALTER TABLE user_answers ADD CONSTRAINT user_answers_answer_id_fkey 
    FOREIGN KEY (answer_id) REFERENCES answers(id) ON DELETE SET NULL;

ALTER TABLE user_answers DROP CONSTRAINT IF EXISTS fk_user_answers_question;
ALTER TABLE user_answers DROP CONSTRAINT IF EXISTS user_answers_question_id_fkey;
ALTER TABLE user_answers ADD CONSTRAINT user_answers_question_id_fkey 
    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE SET NULL;

ALTER TABLE user_answers DROP CONSTRAINT IF EXISTS fk_user_answers_test;
ALTER TABLE user_answers DROP CONSTRAINT IF EXISTS user_answers_test_id_fkey;
ALTER TABLE user_answers ADD CONSTRAINT user_answers_test_id_fkey 
    FOREIGN KEY (test_id) REFERENCES tests(id) ON DELETE SET NULL;

-- User Answers: Add snapshot columns
ALTER TABLE user_answers ADD COLUMN IF NOT EXISTS test_title_snapshot VARCHAR(255);
ALTER TABLE user_answers ADD COLUMN IF NOT EXISTS question_content_snapshot TEXT;
ALTER TABLE user_answers ADD COLUMN IF NOT EXISTS answer_content_snapshot TEXT;
ALTER TABLE user_answers ADD COLUMN IF NOT EXISTS is_correct_snapshot BOOLEAN;

-- Lecture Comments: SET NULL (preserve comments when lecture deleted)
ALTER TABLE lecture_comments DROP CONSTRAINT IF EXISTS fkiekvglp3glcxt5xsbip7julm9;
ALTER TABLE lecture_comments DROP CONSTRAINT IF EXISTS lecture_comments_lecture_id_fkey;
ALTER TABLE lecture_comments ALTER COLUMN lecture_id DROP NOT NULL;
ALTER TABLE lecture_comments ADD CONSTRAINT lecture_comments_lecture_id_fkey 
    FOREIGN KEY (lecture_id) REFERENCES lectures(id) ON DELETE SET NULL;

-- Lecture Comments: Add snapshot columns
ALTER TABLE lecture_comments ADD COLUMN IF NOT EXISTS lecture_title_snapshot VARCHAR(255);
ALTER TABLE lecture_comments ADD COLUMN IF NOT EXISTS course_id VARCHAR(255);
CREATE INDEX IF NOT EXISTS idx_lecture_comments_course_id ON lecture_comments(course_id);

-- Backfill course_id for existing comments
UPDATE lecture_comments lc
SET course_id = ch.course_id
FROM lectures l
JOIN chapters ch ON ch.id = l.chapter_id
WHERE lc.lecture_id = l.id AND lc.course_id IS NULL;

-- Backfill lecture_title_snapshot for existing comments  
UPDATE lecture_comments lc
SET lecture_title_snapshot = l.title
FROM lectures l
WHERE lc.lecture_id = l.id AND lc.lecture_title_snapshot IS NULL;

-- Lecture Comment Reactions: CASCADE
ALTER TABLE lecture_comment_reactions DROP CONSTRAINT IF EXISTS fk_comment_reaction_comment;
ALTER TABLE lecture_comment_reactions ADD CONSTRAINT fk_comment_reaction_comment 
    FOREIGN KEY (comment_id) REFERENCES lecture_comments(id) ON DELETE CASCADE;

-- Feedbacks: SET NULL (preserve reviews when course deleted)
ALTER TABLE feedbacks DROP CONSTRAINT IF EXISTS fk_feedbacks_course;
ALTER TABLE feedbacks DROP CONSTRAINT IF EXISTS feedbacks_course_id_fkey;
ALTER TABLE feedbacks ALTER COLUMN course_id DROP NOT NULL;
ALTER TABLE feedbacks ADD CONSTRAINT feedbacks_course_id_fkey 
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE SET NULL;

ALTER TABLE feedbacks ADD COLUMN IF NOT EXISTS course_title_snapshot VARCHAR(255);

-- Enrollments: Add wallet_address
ALTER TABLE enrollments ADD COLUMN IF NOT EXISTS wallet_address VARCHAR(255);

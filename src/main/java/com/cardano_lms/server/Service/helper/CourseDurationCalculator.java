package com.cardano_lms.server.Service.helper;

import com.cardano_lms.server.Entity.Chapter;
import com.cardano_lms.server.Entity.Course;
import com.cardano_lms.server.Entity.Lecture;
import com.cardano_lms.server.Entity.Test;

public final class CourseDurationCalculator {
    
    private CourseDurationCalculator() {}

    public static int calculateTotalMinutes(Course course) {
        if (course == null) return 0;
        return calculateChaptersDuration(course) + calculateCourseTestsDuration(course);
    }

    private static int calculateChaptersDuration(Course course) {
        if (course.getChapters() == null) return 0;
        
        int total = 0;
        for (Chapter chapter : course.getChapters()) {
            total += calculateLecturesDuration(chapter);
            total += calculateChapterTestsDuration(chapter);
        }
        return total;
    }

    private static int calculateLecturesDuration(Chapter chapter) {
        if (chapter.getLectures() == null) return 0;
        
        int total = 0;
        for (Lecture lecture : chapter.getLectures()) {
            total += lecture.getTime();
        }
        return total;
    }

    private static int calculateChapterTestsDuration(Chapter chapter) {
        if (chapter.getTests() == null) return 0;
        
        int total = 0;
        for (Test test : chapter.getTests()) {
            total += test.getDurationMinutes();
        }
        return total;
    }

    private static int calculateCourseTestsDuration(Course course) {
        if (course.getCourseTests() == null) return 0;
        
        int total = 0;
        for (Test test : course.getCourseTests()) {
            total += test.getDurationMinutes();
        }
        return total;
    }
}

CREATE INDEX idx_image_filepath_contentid
    ON image (file_path, content_id);

CREATE INDEX idx_image_uploaded_at
    ON image (uploaded_at);
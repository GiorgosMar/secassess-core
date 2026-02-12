package org.secassess.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data transfer object providing analytical feedback and statistics regarding the outcome of a criteria copy operation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CopyStatsResponseDto {
    private int copied;
    private int skippedDuplicates;
    private int filteredOut;
    private int totalSource;
}
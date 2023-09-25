package top.ysqorz.batch.springbatch.batch;

import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.validator.BeanValidatingItemProcessor;
import org.springframework.batch.item.validator.ValidationException;
import top.ysqorz.batch.springbatch.model.vo.RowVO;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2023/9/25
 */
public class ValidateItemProcessor<T extends RowVO> extends BeanValidatingItemProcessor<T> {
    private StepExecution stepExecution;

    @Override
    public T process(T item) throws ValidationException {
        try {
            return super.process(item);
        } catch (ValidationException ex) {
            stepExecution.getExecutionContext().put(item.getRowKey(), ex.getMessage());
            return null; // 过滤掉
        }
    }

    @BeforeStep
    public void beforeStep(StepExecution stepExecution) {
        this.stepExecution = stepExecution;
    }
}

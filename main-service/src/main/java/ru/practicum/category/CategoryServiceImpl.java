package ru.practicum.category;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.CategoryMapper;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.event.Event;
import ru.practicum.event.EventRepository;
import ru.practicum.exceptions.CategoryNotFoundException;
import ru.practicum.exceptions.ForbiddenException;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.exceptions.ValidationRequestException;

import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.category.dto.CategoryMapper.toCategory;
import static ru.practicum.category.dto.CategoryMapper.toCategoryDto;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    @Override
    public List<CategoryDto> getCategories(int from, int size) {
        log.info("Получение списка категорий: from = " + from + ", size = " + size);
        return categoryRepository.findAll(PageRequest.of(from / size, size))
                .stream()
                .map(CategoryMapper::toCategoryDto)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDto getCategoryById(long catId) {
        log.info("Получение информации о категории с ID: cat_id = " + catId);
        return toCategoryDto(categoryRepository.findById(catId)
                .orElseThrow(() -> new CategoryNotFoundException(catId)));
    }

    @Override
    @Transactional
    public CategoryDto createCategory(NewCategoryDto newCategoryDto) {
        log.info("Добавление новой категории: category name = " + newCategoryDto);
        if (categoryRepository.existsByName(newCategoryDto.getName())) {
            throw new DataIntegrityViolationException("Категория с таким именем уже существует");
        }

        return toCategoryDto(categoryRepository.save(toCategory(newCategoryDto)));
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(long catId, NewCategoryDto newCategoryDto) {
        log.info("Обновление категории: cat_id = " + catId + ", category name = " + newCategoryDto);
        Category existCategory = categoryRepository.findById(catId)
                .orElseThrow(() -> new CategoryNotFoundException(catId));

        if (!existCategory.getName().equals(newCategoryDto.getName()) &&
                categoryRepository.existsByName(newCategoryDto.getName())) {
            throw new DataIntegrityViolationException("Категория с таким именем уже существует");
        }

        existCategory.setName(newCategoryDto.getName());
        return toCategoryDto(categoryRepository.save(existCategory));
    }

    @Override
    @Transactional
    public void deleteCategory(long catId) {
        log.info("Удаление категории: cat_id = " + catId);
        categoryRepository.findById(catId)
                .orElseThrow(() -> new CategoryNotFoundException(catId));
        Event event = eventRepository.findFirstByCategoryId(catId);
        if (event != null) {
            throw new ForbiddenException("Категория не пустая");
        }
        categoryRepository.deleteById(catId);
    }
}
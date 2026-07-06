package com.qst.onlinenewsbackend.controller;

import com.qst.onlinenewsbackend.common.Result;
import com.qst.onlinenewsbackend.entity.Category;
import com.qst.onlinenewsbackend.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类控制器
 * 模块1：新闻列表展示与分类浏览
 */
@RestController
@RequestMapping("/api/category")
@Tag(name = "分类管理", description = "新闻分类相关接口")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/list")
    @Operation(summary = "获取分类列表", description = "获取所有新闻分类，按sortOrder排序")
    public Result<?> getCategoryList() {
        List<Category> list = categoryService.lambdaQuery()
                .orderByAsc(Category::getSortOrder)
                .list();
        return Result.success(list);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取分类详情", description = "根据分类ID获取分类信息")
    public Result<?> getCategoryById(@PathVariable Integer id) {
        Category category = categoryService.getById(id);
        if (category == null) {
            return Result.error("分类不存在");
        }
        return Result.success(category);
    }

    @PostMapping
    @Operation(summary = "新增分类", description = "管理员新增新闻分类")
    public Result<?> addCategory(@RequestBody Category category) {
        categoryService.save(category);
        return Result.success("新增成功", category);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新分类", description = "管理员更新分类信息")
    public Result<?> updateCategory(@PathVariable Integer id, @RequestBody Category category) {
        category.setId(id);
        categoryService.updateById(category);
        return Result.success("更新成功", category);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除分类", description = "管理员删除新闻分类")
    public Result<?> deleteCategory(@PathVariable Integer id) {
        categoryService.removeById(id);
        return Result.success("删除成功");
    }
}
